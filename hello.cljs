(ns hello
  "The application entry point"
  (:require
   [applied-science.js-interop :as j]
   ["@jackdbd/notifications" :refer [sendTelegramMessage]]))

(defn structured-log
  "Log an entry using structured logging.
   
   https://cloud.google.com/functions/docs/monitoring/logging#writing_structured_logs
   https://cloud.google.com/logging/docs/reference/v2/rest/v2/LogEntry?authuser=1#logseverity"
  [{:keys [message severity]}]
  (let [entry #js {:message message :severity severity}]
    ;; TODO:
    ;; None of these is printing a jsonPayload in Cloud Logging.
    ;; All of these are printing a textPayload. Why?
    ;; (js/console.log (js/JSON.stringify #js {"message" message "severity" severity}))
    ;; (js/console.log (js/JSON.stringify entry))
    (js/console.log entry)))

(defn make-handle-error
  [res]
  (fn [err]
    (js/console.error (j/get err :message))
    (.status res 500)
    (.send res #js {:message (j/get err :message)})))

(defn hello-cljs
  "The function to execute"
  [req res]
  (let [handle-error (make-handle-error res)
        NODE_ENV (j/get-in js/process [:env "NODE_ENV"])
        TELEGRAM (j/get-in js/process [:env "TELEGRAM"])]

    (when (nil? NODE_ENV)
      (throw (js/Error. "environment variable NODE_ENV not set")))
    (when (nil? TELEGRAM)
      (throw (js/Error. "environment variable TELEGRAM not set")))

    (js/console.debug "process.env.NODE_ENV:" (j/get-in js/process [:env "NODE_ENV"]))
    (js/console.info "got request from User-Agent:" (j/get-in req [:headers :user-agent]))
    (comment
      (js/console.debug "Request Body:" (j/get req :body)))

    (let [text (j/get-in req [:body :text])]
      (if (nil? text)
        (do
          (.status res 400)
          (.send res #js {:message "`text` not set"}))
        (let [m (js/JSON.parse TELEGRAM)
              _credentials (j/select-keys m ["chat_id" "token"])
              chat-id (j/get m "chat_id")
              token (j/get m "token")
              config #js {"chat_id" chat-id
                          "token" token
                          "text" text}
              options #js {"disable_notification" true
                           "disable_web_page_preview" false
                           "parse_mode" "HTML"}]

          (-> (sendTelegramMessage config options)
              (.then (fn [telegram-response]
                       (let [delivered (j/get telegram-response :delivered)
                             delivered-at (j/get telegram-response "delivered_at")
                             message (j/get telegram-response :message)]
                      ;;  (js/console.debug "telegram-response" telegram-response)
                         (if delivered
                           (do
                             (structured-log {:severity "INFO" :message (str "message delivered to chat" chat-id "at" delivered-at)})
                             (.status res 200)
                             (.send res #js {:delivered delivered :delivered-at delivered-at :message message}))
                           (let [msg (str "message not delivered to chat" chat-id)]
                             (structured-log {:severity "WARN" :message msg})
                             (.status res 200)
                             (.send res #js {:delivered delivered :message msg}))))))
              (.catch handle-error)))))))

#js {:hello hello-cljs}
