/*
 *  Copyright (c) Lightstreamer Srl
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.lightstreamer.adapters.remote;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lightstreamer.log.LogManager;
import com.lightstreamer.log.Logger;

class SubscriptionHelper {
    private static Logger _log = LogManager.getLogger("com.lightstreamer.adapters.remote.Server.DataProviderServer");

    private Map<String,SubscrData> _activeItems;

    private final String _poolType;
    private final ExecutorService _executor;
    
    public SubscriptionHelper() {
        _activeItems = new HashMap<String,SubscrData>();
        
        String threadsConf = System.getProperty("lightstreamer.data.pool.size");
        if (threadsConf == null) {
            threadsConf = "0";
        }
        try {
            int threads = Integer.parseInt(threadsConf);
            if (threads <= 0) {
                _poolType = "unlimited thread pool";
                _executor = Executors.newCachedThreadPool();
            } else if (threads == 1) {
                _poolType = "sequential invocations";
                _executor = Executors.newSingleThreadExecutor();
            } else {
                _poolType = "thread pool size = " + threads;
                _executor = Executors.newFixedThreadPool(threads);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid lightstreamer.data.pool.size configuration: " + threadsConf);
        }
    }
    
    public void shutdown() {
        _executor.shutdown();
    }

    public final void doSubscription(String itemName, Task subscriptionTask) {
        // we are still in the request dequeueing thread,
        // hence we know that the invocations of doSubscription
        // and doUnsubscription are sequential for the same item;
        // to prevent blocking this thread, we will invoke the
        // subscribe() on the Adapter in a different thread;
        // but we will enqueue the requests pertaining to the same
        // item, so as to guarantee sequentiality to the Adapter too

        SubscrData data;
        synchronized (_activeItems) {
            data = _activeItems.get(itemName);
            if (data == null) {
                data = new SubscrData(this, itemName);
                _activeItems.put(itemName, data);
            }
            data._queued++;
                // questo impedisce la rimozione dell'elemento
                // se il thread scodatore finisce proprio adesso
        }
        data.addTask(subscriptionTask, true);
    }

    public final void doUnsubscription(String itemName, Task subscriptionTask) {
        // we are still in the request dequeueing thread,
        // hence we know that the invocations of doSubscription
        // and doUnsubscription are sequential for the same item;
        // to prevent blocking this thread, we will invoke the
        // unsubscribe() on the Adapter in a different thread;
        // but we will enqueue the requests pertaining to the same
        // item, so as to guarantee sequentiality to the Adapter too

        SubscrData data;
        synchronized (_activeItems) {
            data =_activeItems.get(itemName);
            if (data == null) {
                // impossible, unless the corresponding subscription request
                // got lost; in fact, it should have created the element
                // and set _queued; and the dequeuer can have reset _queued
                // only after setting _code; under such conditions,
                // the element cannot have been eliminated
                _log.error("Task list expected for item " + itemName);
                return;
            }
            data._queued++;
                // this would prevent the removal of the element
                // in case the dequeuing thread should end right now
        }
        data.addTask(subscriptionTask, false);
    }

    public final String getSubscriptionCode(String itemName) {
        synchronized (_activeItems) {
            SubscrData data = _activeItems.get(itemName);
            if (data != null) {
                return data._code;
                    // it may be null, in case an unsubscription
                    // has just finished but a new subscription
                    // has already been enqueued
            } else {
                return null;
            }
        }
    }
    
    public String getPoolType() {
        return _poolType;
    }

    private static class SubscrData {
        public SubscriptionHelper _container;
        public String _itemName;
        public int _queued; // will be synchronized with items
        public String _code; // will be synchronized with items
        public LinkedList<Task> _tasks;
        public boolean _subscrExpected;
        public boolean _running;
        public boolean _lastSubscrOutcome;

        public SubscrData(SubscriptionHelper container, String itemName) {
            _container = container;
            _itemName = itemName;
            _tasks = new LinkedList<Task>();
            _subscrExpected = true;
            _queued = 0;
            _code = null;
            _running = false;
        }

        public final void addTask(Task task, boolean isSubscr) {
            if (isSubscr != (task.getCode() != null)) {
                // impossible, unless DataProviderServer were bugged
                _log.error("Inconsistent task for item " + _itemName);
            }
            synchronized (this) {
                if (isSubscr != _subscrExpected) {
                    // impossible, unless the sequence of requests
                    // to the Remote Server were wrong
                    _log.error("Unexpected task for item " + _itemName);
                }
                _tasks.offer(task);
                    // _queued has already been incremented by the caller
                _subscrExpected = !isSubscr;
                if (!_running) {
                    // only one dequeuer can be active
                    _running = true;
                    _container._executor.submit(new Runnable() {
                        public void run() {
                            dequeue();
                        }
                    });
                }
            }
        }

        public final void dequeue() {
            int dequeued = 0;
            boolean lastSubscrOutcome = true;
                // it will be first taken from the state anyway
            while (true) {
                Task task;
                boolean isLast;
                synchronized (this) {
                    if (dequeued == 0) {
                        lastSubscrOutcome = _lastSubscrOutcome;
                            // initial state
                    }
                    if (_tasks.isEmpty()) {
                        _lastSubscrOutcome = lastSubscrOutcome;
                            // final state
                        _running = false;
                        break;
                        // from this moment it is possible that a new
                        // dequeuer gets started
                    }
                    task = _tasks.poll();
                    isLast = (_tasks.isEmpty());
                    dequeued++;
                }
                // we will invoke the subscribe/unsubscribe without holding the lock
                try {
                    String code = task.getCode();
                    if (code != null) {
                        // IT'S A SUBSCRIPTION
                        // ASSERT (either it is the first event, or it is preceded by an unsubscription)
                        if (!isLast) {
                            // ASSERT (it will be followed by an unsubscription)
                            task.doLateTask();
                            lastSubscrOutcome = false;
                                // on the next iteration we will dequeue the unsubscription,
                                // again with doLateTask
                        } else {
                            synchronized (_container._activeItems) {
                                _code = code;
                                // from this moment, the received updates will be
                                // associated with this subscription; should we receive
                                // late updates meant for a previous subscription,
                                // they would be misinterpreted; it's the Adapter
                                // responsible for ensuring that this never happens,
                                // that is, that no update for this item is sent after
                                // the termination of the unsubscribe() invocation
                            }
                            lastSubscrOutcome = task.doTask();
                                // if it return false, i.e. the subscription
                                // has failed, we won't invoke unsubscribe()
                        }
                    } else {
                        // IT'S AN UNSUBSCRIPTION
                        // ASSERT(the event was preceded by a subscription)
                        if (lastSubscrOutcome) {
                            task.doTask();
                            // we don't care if it was successful or not;
                            // an unsuccessful unsubscribe doesn't propagate effects
                        } else {
                            // either the previous subscription failed
                            // or it was obsolete and not invoked at all
                            task.doLateTask();
                        }
                        synchronized (_container._activeItems) {
                            _code = null;
                            // from this moment any update received from the Adapter
                            // will be ignored; however, the Adapter should ensure
                            // that no update for this item is sent after
                            // the termination of the unsubscribe() invocation
                        }
                    }
                } catch (RemotingException e) {
                    _log.error("Unexpected error: " + e.getMessage(),e);
                } 
            }

            synchronized (_container._activeItems) {
                _queued -= dequeued;
                // as long as the item is subscribed to, the element should be kept;
                // if the item was unsubscribed from, the element should be removed,
                // unless a new subscription request has already been received;
                // in the latter case, _queued cannot be zero
                if (_code == null && _queued == 0) {
                    SubscrData data = _container._activeItems.get(_itemName);
                    if (data == null) {
                        // it can happen, in case this dequeueing thread
                        // was stopped just above and has been preceded
                        // by a new subscribe/unsubscribe pair
                        // with a related new dequeueing thread
                    } else if (data != this) {
                        // event this can happen, if, in the above case,
                        // also the second thread was stopped just above
                        // and has been preceded by a new subscribe/unsubscribe pair
                        // with a related new dequeueing thread
                    } else {
                        _container._activeItems.remove(_itemName);
                    }
                } else {
                    // we can exit safely, because new events are bound to come
                    // or to be dequeued by a new dequeueing thread,
                    // which will have a new opportunity to remove this element
                }
            }
        }
    }
}