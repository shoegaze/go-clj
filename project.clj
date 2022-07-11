(defproject go-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}

  :source-paths ["src/clj"
                 "src/cljs"]

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.758"]
                 [ring/ring-core "1.9.5"]
                 [ring/ring-jetty-adapter "1.9.5"]
                 [compojure "1.7.0"]
                 [hiccup "1.0.5"]
                 [domina "1.0.3"]
                 [cljsjs/react "17.0.2-0"]
                 [cljsjs/react-dom "17.0.2-0"]
                 [reagent "1.1.1"]]

  :plugins [[lein-cljsbuild "1.1.8"]]

  :cljsbuild {:builds
              {:dev
               {:source-paths ["src/cljs"]
                :compiler {:output-to "resources/public/js/bundle/bundle.dev.js"
                           :optimizations :whitespace
                           :pretty-print true}}
               :pre
               {:source-paths ["src/cljs"]
                :compiler {:output-to "resources/public/js/bundle/bundle.pre.js"
                           :optimizations :simple
                           :pretty-print false}}
               :prod
               {:source-paths ["src/cljs"]
                :compiler {:output-to "resources/public/js/bundle/bundle.prod.js"
                           :optimizations :advanced
                           :pretty-print false}}}}

  :clean-targets ^{:protect false} [:target-path "resources/public/js/bundle/"]

  :aliases {"serve" ["run" "-m" "server.core"]
            "serve-repl" ["trampoline" "cljsbuild" "repl-listen"]
            "client-rebuild-all" ["do" "clean,"
                                  "cljsbuild" "once"]
            "client-build-dev" ["cljsbuild" "once" "dev"]
            "client-build-pre" ["cljsbuild" "once" "pre"]
            "client-build-prod" ["cljsbuild" "once" "prod"]}

  :repl-options {:init-ns server.core})
