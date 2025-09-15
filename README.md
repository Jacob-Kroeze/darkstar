# Darkstar

This is an experimental fork of [Applied Science's Darkstar](https://github.com/applied-science/darkstar) library, extending it to support additional visualization libraries beyond Vega and Vega-lite.

The original Darkstar packages Vega `5.10.1` and Vega-lite `4.10.1` as a single dependency Clojure library. This fork adds experimental support for D3.js, Observable Plot, and simple diagram generation while maintaining the same small API surface.

This was made possible by the GraalJS Javascript runtime, which works on any stock JVM >= `1.8.0_131`. Tested on HotSpot, OpenJDK 11, 13, and 21.

## Installation

Add to deps.edn:

``` clojure
daslu/darkstar {:git/url "https://github.com/daslu/darkstar/"
                :sha "latest-sha"}
```

## Usage

``` clojure
(ns test
  (:require [applied-science.darkstar :as darkstar]))

;; Vega and Vega-lite (original functionality)
(->> (slurp "vega-example.json")
     darkstar/vega-spec->svg
     (spit "vg-example.svg"))

(->> (slurp "vega-lite-example.json")
     darkstar/vega-lite-spec->svg
     (spit "vl-example.svg"))

;; D3.js scripts (experimental)
(->> custom-d3-script
     darkstar/d3-script->svg
     (spit "d3-chart.svg"))

;; Observable Plot (experimental)
(->> plot-specification
     darkstar/plot->svg
     (spit "plot-chart.svg"))

;; Simple diagrams (experimental)
(-> (darkstar/simple-flowchart nodes connections)
    (spit "flowchart.svg"))
```

See [examples](src/applied_science/darkstar_examples.clj) for complete working examples of all supported visualization types.

Additional libraries (Plotly.js, Apache ECharts, Viz.js (Graphviz), Mermaid) were attempted but proved incompatible with GraalJS and are available in separate experimental branches.

## Development

Run tests:

    $ clojure -M:test:runner

Start REPL:

    $ clojure -M:nrepl

Build and deploy:

    $ clojure -M:release
    $ clojure -M:release deploy

## Attribution

Original Darkstar: Copyright © 2020 Applied Science  
Fork modifications: Copyright © 2025 Daniel Slutsky  
Distributed under the MIT License.
