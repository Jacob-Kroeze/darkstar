(ns applied-science.darkstar-examples
  "Working examples for the Darkstar library demonstrating Vega and Vega-lite rendering to SVG"
  (:require [applied-science.darkstar :as darkstar]
            [clojure.java.io :as io]))

;; ============================================================================
;; Basic Examples
;; ============================================================================

(defn simple-bar-chart
  "Renders a basic bar chart using pure Vega specification"
  []
  (let [spec "{
    \"$schema\": \"https://vega.github.io/schema/vega/v5.json\",
    \"width\": 400,
    \"height\": 200,
    \"padding\": 5,
    \"data\": [
      {
        \"name\": \"table\",
        \"values\": [
          {\"category\": \"A\", \"amount\": 28},
          {\"category\": \"B\", \"amount\": 55},
          {\"category\": \"C\", \"amount\": 43},
          {\"category\": \"D\", \"amount\": 91},
          {\"category\": \"E\", \"amount\": 81},
          {\"category\": \"F\", \"amount\": 53}
        ]
      }
    ],
    \"scales\": [
      {
        \"name\": \"xscale\",
        \"type\": \"band\",
        \"domain\": {\"data\": \"table\", \"field\": \"category\"},
        \"range\": \"width\",
        \"padding\": 0.05,
        \"round\": true
      },
      {
        \"name\": \"yscale\",
        \"domain\": {\"data\": \"table\", \"field\": \"amount\"},
        \"nice\": true,
        \"range\": \"height\"
      }
    ],
    \"axes\": [
      {\"orient\": \"bottom\", \"scale\": \"xscale\"},
      {\"orient\": \"left\", \"scale\": \"yscale\"}
    ],
    \"marks\": [
      {
        \"type\": \"rect\",
        \"from\": {\"data\": \"table\"},
        \"encode\": {
          \"enter\": {
            \"x\": {\"scale\": \"xscale\", \"field\": \"category\"},
            \"width\": {\"scale\": \"xscale\", \"band\": 1},
            \"y\": {\"scale\": \"yscale\", \"field\": \"amount\"},
            \"y2\": {\"scale\": \"yscale\", \"value\": 0}
          },
          \"update\": {
            \"fill\": {\"value\": \"steelblue\"}
          },
          \"hover\": {
            \"fill\": {\"value\": \"orange\"}
          }
        }
      }
    ]
  }"]
    (darkstar/vega-spec->svg spec)))

(defn simple-vega-lite-bar
  "Renders a bar chart using the simpler Vega-lite specification"
  []
  (let [spec "{
    \"$schema\": \"https://vega.github.io/schema/vega-lite/v4.json\",
    \"description\": \"A simple bar chart with embedded data.\",
    \"width\": 400,
    \"data\": {
      \"values\": [
        {\"category\": \"A\", \"value\": 28},
        {\"category\": \"B\", \"value\": 55},
        {\"category\": \"C\", \"value\": 43},
        {\"category\": \"D\", \"value\": 91},
        {\"category\": \"E\", \"value\": 81},
        {\"category\": \"F\", \"value\": 53}
      ]
    },
    \"mark\": \"bar\",
    \"encoding\": {
      \"x\": {
        \"field\": \"category\",
        \"type\": \"nominal\",
        \"axis\": {\"labelAngle\": 0}
      },
      \"y\": {
        \"field\": \"value\",
        \"type\": \"quantitative\"
      }
    }
  }"]
    (darkstar/vega-lite-spec->svg spec)))

;; ============================================================================
;; More Complex Examples
;; ============================================================================

(defn scatter-plot
  "Creates a scatter plot with color encoding"
  []
  (let [spec "{
    \"$schema\": \"https://vega.github.io/schema/vega-lite/v4.json\",
    \"description\": \"A scatter plot with color encoding\",
    \"width\": 400,
    \"height\": 300,
    \"data\": {
      \"values\": [
        {\"x\": 1, \"y\": 2, \"category\": \"A\"},
        {\"x\": 2, \"y\": 4, \"category\": \"B\"},
        {\"x\": 3, \"y\": 3, \"category\": \"A\"},
        {\"x\": 4, \"y\": 5, \"category\": \"B\"},
        {\"x\": 5, \"y\": 4, \"category\": \"A\"},
        {\"x\": 6, \"y\": 7, \"category\": \"B\"},
        {\"x\": 7, \"y\": 6, \"category\": \"A\"},
        {\"x\": 8, \"y\": 9, \"category\": \"B\"}
      ]
    },
    \"mark\": {
      \"type\": \"point\",
      \"size\": 100,
      \"filled\": true
    },
    \"encoding\": {
      \"x\": {
        \"field\": \"x\",
        \"type\": \"quantitative\",
        \"scale\": {\"domain\": [0, 10]}
      },
      \"y\": {
        \"field\": \"y\",
        \"type\": \"quantitative\",
        \"scale\": {\"domain\": [0, 10]}
      },
      \"color\": {
        \"field\": \"category\",
        \"type\": \"nominal\",
        \"scale\": {\"scheme\": \"category10\"}
      }
    }
  }"]
    (darkstar/vega-lite-spec->svg spec)))

(defn line-chart
  "Creates a multi-series line chart"
  []
  (let [spec "{
    \"$schema\": \"https://vega.github.io/schema/vega-lite/v4.json\",
    \"description\": \"Multi-series line chart\",
    \"width\": 400,
    \"height\": 300,
    \"data\": {
      \"values\": [
        {\"x\": 1, \"y\": 10, \"series\": \"A\"},
        {\"x\": 2, \"y\": 25, \"series\": \"A\"},
        {\"x\": 3, \"y\": 30, \"series\": \"A\"},
        {\"x\": 4, \"y\": 35, \"series\": \"A\"},
        {\"x\": 5, \"y\": 40, \"series\": \"A\"},
        {\"x\": 1, \"y\": 15, \"series\": \"B\"},
        {\"x\": 2, \"y\": 20, \"series\": \"B\"},
        {\"x\": 3, \"y\": 35, \"series\": \"B\"},
        {\"x\": 4, \"y\": 30, \"series\": \"B\"},
        {\"x\": 5, \"y\": 45, \"series\": \"B\"}
      ]
    },
    \"mark\": \"line\",
    \"encoding\": {
      \"x\": {
        \"field\": \"x\",
        \"type\": \"quantitative\",
        \"axis\": {\"title\": \"X Axis\"}
      },
      \"y\": {
        \"field\": \"y\",
        \"type\": \"quantitative\",
        \"axis\": {\"title\": \"Y Axis\"}
      },
      \"color\": {
        \"field\": \"series\",
        \"type\": \"nominal\"
      }
    }
  }"]
    (darkstar/vega-lite-spec->svg spec)))

(defn heatmap
  "Creates a heatmap visualization"
  []
  (let [spec "{
    \"$schema\": \"https://vega.github.io/schema/vega-lite/v4.json\",
    \"width\": 300,
    \"height\": 200,
    \"data\": {
      \"values\": [
        {\"x\": \"A\", \"y\": \"1\", \"value\": 28},
        {\"x\": \"A\", \"y\": \"2\", \"value\": 55},
        {\"x\": \"A\", \"y\": \"3\", \"value\": 43},
        {\"x\": \"B\", \"y\": \"1\", \"value\": 91},
        {\"x\": \"B\", \"y\": \"2\", \"value\": 81},
        {\"x\": \"B\", \"y\": \"3\", \"value\": 53},
        {\"x\": \"C\", \"y\": \"1\", \"value\": 19},
        {\"x\": \"C\", \"y\": \"2\", \"value\": 87},
        {\"x\": \"C\", \"y\": \"3\", \"value\": 52}
      ]
    },
    \"mark\": \"rect\",
    \"encoding\": {
      \"x\": {
        \"field\": \"x\",
        \"type\": \"nominal\",
        \"axis\": {\"title\": \"Category\"}
      },
      \"y\": {
        \"field\": \"y\",
        \"type\": \"nominal\",
        \"axis\": {\"title\": \"Group\"}
      },
      \"color\": {
        \"field\": \"value\",
        \"type\": \"quantitative\",
        \"scale\": {\"scheme\": \"blues\"}
      }
    }
  }"]
    (darkstar/vega-lite-spec->svg spec)))

;; ============================================================================
;; Data Transformation Examples
;; ============================================================================

(defn aggregated-bar-chart
  "Bar chart with data aggregation"
  []
  (let [spec "{
    \"$schema\": \"https://vega.github.io/schema/vega-lite/v4.json\",
    \"description\": \"Bar chart with aggregation\",
    \"width\": 400,
    \"data\": {
      \"values\": [
        {\"category\": \"A\", \"group\": \"x\", \"value\": 10},
        {\"category\": \"A\", \"group\": \"y\", \"value\": 15},
        {\"category\": \"A\", \"group\": \"z\", \"value\": 12},
        {\"category\": \"B\", \"group\": \"x\", \"value\": 20},
        {\"category\": \"B\", \"group\": \"y\", \"value\": 25},
        {\"category\": \"B\", \"group\": \"z\", \"value\": 22},
        {\"category\": \"C\", \"group\": \"x\", \"value\": 30},
        {\"category\": \"C\", \"group\": \"y\", \"value\": 28},
        {\"category\": \"C\", \"group\": \"z\", \"value\": 35}
      ]
    },
    \"mark\": \"bar\",
    \"encoding\": {
      \"x\": {
        \"field\": \"category\",
        \"type\": \"nominal\"
      },
      \"y\": {
        \"aggregate\": \"sum\",
        \"field\": \"value\",
        \"type\": \"quantitative\",
        \"axis\": {\"title\": \"Total Value\"}
      },
      \"color\": {
        \"field\": \"group\",
        \"type\": \"nominal\"
      }
    }
  }"]
    (darkstar/vega-lite-spec->svg spec)))

(defn histogram
  "Creates a histogram with binning"
  []
  (let [spec "{
    \"$schema\": \"https://vega.github.io/schema/vega-lite/v4.json\",
    \"width\": 400,
    \"height\": 200,
    \"data\": {
      \"values\": [
        {\"value\": 1}, {\"value\": 2}, {\"value\": 2}, {\"value\": 3},
        {\"value\": 3}, {\"value\": 3}, {\"value\": 4}, {\"value\": 4},
        {\"value\": 4}, {\"value\": 4}, {\"value\": 5}, {\"value\": 5},
        {\"value\": 5}, {\"value\": 5}, {\"value\": 5}, {\"value\": 6},
        {\"value\": 6}, {\"value\": 6}, {\"value\": 7}, {\"value\": 7},
        {\"value\": 8}, {\"value\": 8}, {\"value\": 8}, {\"value\": 9}
      ]
    },
    \"mark\": \"bar\",
    \"encoding\": {
      \"x\": {
        \"bin\": {\"maxbins\": 10},
        \"field\": \"value\",
        \"type\": \"quantitative\"
      },
      \"y\": {
        \"aggregate\": \"count\",
        \"type\": \"quantitative\"
      }
    }
  }"]
    (darkstar/vega-lite-spec->svg spec)))

;; ============================================================================
;; Helper Functions
;; ============================================================================

(defn save-example
  "Saves an SVG string to a file"
  [svg-string filename]
  (spit (str "examples/" filename) svg-string)
  (println (str "Saved to examples/" filename)))

(defn generate-all-examples
  "Generates all example visualizations and saves them to files"
  []
  (io/make-parents "examples/dummy.txt")
  (println "Generating all examples...")

  (save-example (simple-bar-chart) "simple-bar.svg")
  (save-example (simple-vega-lite-bar) "vega-lite-bar.svg")
  (save-example (scatter-plot) "scatter-plot.svg")
  (save-example (line-chart) "line-chart.svg")
  (save-example (heatmap) "heatmap.svg")
  (save-example (aggregated-bar-chart) "aggregated-bar.svg")
  (save-example (histogram) "histogram.svg")

  (println "All examples generated!"))

;; ============================================================================
;; Usage Examples in Comments
;; ============================================================================

(comment
  ;; Load this namespace in REPL
  (require '[applied-science.darkstar-examples :as examples])

  ;; Generate a simple bar chart
  (def my-chart (examples/simple-bar-chart))
  (spit "my-chart.svg" my-chart)

  ;; Generate all examples
  (examples/generate-all-examples)

  ;; Work with file paths
  (binding [darkstar/*base-directory* "/path/to/data/"]
    (darkstar/vega-spec->svg spec-with-relative-paths))

  ;; Convert your own spec
  (-> (slurp "my-spec.vl.json")
      darkstar/vega-lite-spec->svg
      (spit "my-output.svg")))