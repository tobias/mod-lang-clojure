(ns vertx.deploy-test
  (:require [vertx.testtools :as t]
            [vertx.core :as core]
            [vertx.eventbus :as eb]))

(defn test-deploy []
  (eb/register-handler
   "test.data"
   (fn [m]
     (t/test-complete
      (t/assert= "started" (.body m)))))
  
  (core/deploy-verticle "deploy/child.clj" {:ham "biscuit"}))

(defn test-deploy-with-handler []
  (core/deploy-verticle
   "deploy/child.clj" {:ham "biscuit"} 1
   (fn [err id]
     (t/test-complete
      (t/assert-nil err)
      (t/assert-not-nil id)))))

(defn test-deploy-failure []
  (core/deploy-verticle
   "deploy/does_not_exist.clj" nil 1
   (fn [err id]
     (t/test-complete
      (t/assert-not-nil err)
      (t/assert-nil id)))))

(defn test-undeploy []
  (eb/register-handler
   "test.data"
   (fn [m]
     (when (= "stopped" (.body m))
       (t/test-complete))))

  (core/deploy-verticle
   "deploy/child.clj" {:ham "biscuit"} 1
   (fn [err id]
     (t/assert-not-nil id)
     (core/undeploy-verticle id))))

(defn test-undeploy-with-handler []
  (core/deploy-verticle
   "deploy/child.clj" {:ham "biscuit"} 1
   (fn [err id]
     (t/assert-not-nil id)
     (core/undeploy-verticle
      id
      (fn [err]
        (t/test-complete
         (t/assert-nil err)))))))

(defn test-undeploy-failure []
  (core/undeploy-verticle
      "not-deployed"
      (fn [err]
        (t/test-complete
         (t/assert-not-nil err)))))

(t/start-tests)

