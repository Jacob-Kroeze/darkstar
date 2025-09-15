(ns applied-science.darkstar-examples
  "Working examples for the Darkstar library demonstrating Vega, Vega-lite, and D3.js rendering to SVG.
   
   This namespace provides comprehensive examples of server-side data visualization using:
   - Vega: Grammar of graphics declarative visualizations
   - Vega-lite: High-level grammar for statistical graphics  
   - D3.js: Low-level imperative DOM manipulation for custom charts
   
   All examples generate SVG output that can be saved to files or embedded in web pages."
  (:require [applied-science.darkstar :as darkstar]
            [charred.api :as charred]
            [clojure.java.io :as io]))

;; ============================================================================
;; Basic Examples
;; ============================================================================

(defn simple-bar-chart
  "Renders a basic bar chart using pure Vega specification"
  []
  (let [spec {:$schema "https://vega.github.io/schema/vega/v5.json"
              :width 400
              :height 200
              :padding 5
              :data [{:name "table"
                      :values [{:category "A" :amount 28}
                               {:category "B" :amount 55}
                               {:category "C" :amount 43}
                               {:category "D" :amount 91}
                               {:category "E" :amount 81}
                               {:category "F" :amount 53}]}]
              :scales [{:name "xscale"
                        :type "band"
                        :domain {:data "table" :field "category"}
                        :range "width"
                        :padding 0.05
                        :round true}
                       {:name "yscale"
                        :domain {:data "table" :field "amount"}
                        :nice true
                        :range "height"}]
              :axes [{:orient "bottom" :scale "xscale"}
                     {:orient "left" :scale "yscale"}]
              :marks [{:type "rect"
                       :from {:data "table"}
                       :encode {:enter {:x {:scale "xscale" :field "category"}
                                        :width {:scale "xscale" :band 1}
                                        :y {:scale "yscale" :field "amount"}
                                        :y2 {:scale "yscale" :value 0}}
                                :update {:fill {:value "steelblue"}}
                                :hover {:fill {:value "orange"}}}}]}]
    (darkstar/vega-spec->svg (charred/write-json-str spec))))

(defn simple-vega-lite-bar
  "Renders a bar chart using the simpler Vega-lite specification"
  []
  (let [spec {:$schema "https://vega.github.io/schema/vega-lite/v4.json"
              :description "A simple bar chart with embedded data."
              :width 400
              :data {:values [{:category "A" :value 28}
                              {:category "B" :value 55}
                              {:category "C" :value 43}
                              {:category "D" :value 91}
                              {:category "E" :value 81}
                              {:category "F" :value 53}]}
              :mark "bar"
              :encoding {:x {:field "category"
                             :type "nominal"
                             :axis {:labelAngle 0}}
                         :y {:field "value"
                             :type "quantitative"}}}]
    (darkstar/vega-lite-spec->svg (charred/write-json-str spec))))

;; ============================================================================
;; More Complex Examples
;; ============================================================================

(defn scatter-plot
  "Creates a scatter plot with color encoding"
  []
  (let [spec {:$schema "https://vega.github.io/schema/vega-lite/v4.json"
              :description "A scatter plot with color encoding"
              :width 400
              :height 300
              :data {:values [{:x 1 :y 2 :category "A"}
                              {:x 2 :y 4 :category "B"}
                              {:x 3 :y 3 :category "A"}
                              {:x 4 :y 5 :category "B"}
                              {:x 5 :y 4 :category "A"}
                              {:x 6 :y 7 :category "B"}
                              {:x 7 :y 6 :category "A"}
                              {:x 8 :y 9 :category "B"}]}
              :mark {:type "point"
                     :size 100
                     :filled true}
              :encoding {:x {:field "x"
                             :type "quantitative"
                             :scale {:domain [0 10]}}
                         :y {:field "y"
                             :type "quantitative"
                             :scale {:domain [0 10]}}
                         :color {:field "category"
                                 :type "nominal"
                                 :scale {:scheme "category10"}}}}]
    (darkstar/vega-lite-spec->svg (charred/write-json-str spec))))

(defn line-chart
  "Creates a multi-series line chart"
  []
  (let [spec {:$schema "https://vega.github.io/schema/vega-lite/v4.json"
              :description "Multi-series line chart"
              :width 400
              :height 300
              :data {:values [{:x 1 :y 10 :series "A"}
                              {:x 2 :y 25 :series "A"}
                              {:x 3 :y 30 :series "A"}
                              {:x 4 :y 35 :series "A"}
                              {:x 5 :y 40 :series "A"}
                              {:x 1 :y 15 :series "B"}
                              {:x 2 :y 20 :series "B"}
                              {:x 3 :y 35 :series "B"}
                              {:x 4 :y 30 :series "B"}
                              {:x 5 :y 45 :series "B"}]}
              :mark "line"
              :encoding {:x {:field "x"
                             :type "quantitative"
                             :axis {:title "X Axis"}}
                         :y {:field "y"
                             :type "quantitative"
                             :axis {:title "Y Axis"}}
                         :color {:field "series"
                                 :type "nominal"}}}]
    (darkstar/vega-lite-spec->svg (charred/write-json-str spec))))

(defn heatmap
  "Creates a heatmap visualization"
  []
  (let [spec {:$schema "https://vega.github.io/schema/vega-lite/v4.json"
              :width 300
              :height 200
              :data {:values [{:x "A" :y "1" :value 28}
                              {:x "A" :y "2" :value 55}
                              {:x "A" :y "3" :value 43}
                              {:x "B" :y "1" :value 91}
                              {:x "B" :y "2" :value 81}
                              {:x "B" :y "3" :value 53}
                              {:x "C" :y "1" :value 19}
                              {:x "C" :y "2" :value 87}
                              {:x "C" :y "3" :value 52}]}
              :mark "rect"
              :encoding {:x {:field "x"
                             :type "nominal"
                             :axis {:title "Category"}}
                         :y {:field "y"
                             :type "nominal"
                             :axis {:title "Group"}}
                         :color {:field "value"
                                 :type "quantitative"
                                 :scale {:scheme "blues"}}}}]
    (darkstar/vega-lite-spec->svg (charred/write-json-str spec))))

;; ============================================================================
;; Data Transformation Examples
;; ============================================================================

(defn aggregated-bar-chart
  "Bar chart with data aggregation"
  []
  (let [spec {:$schema "https://vega.github.io/schema/vega-lite/v4.json"
              :description "Bar chart with aggregation"
              :width 400
              :data {:values [{:category "A" :group "x" :value 10}
                              {:category "A" :group "y" :value 15}
                              {:category "A" :group "z" :value 12}
                              {:category "B" :group "x" :value 20}
                              {:category "B" :group "y" :value 25}
                              {:category "B" :group "z" :value 22}
                              {:category "C" :group "x" :value 30}
                              {:category "C" :group "y" :value 28}
                              {:category "C" :group "z" :value 35}]}
              :mark "bar"
              :encoding {:x {:field "category"
                             :type "nominal"}
                         :y {:aggregate "sum"
                             :field "value"
                             :type "quantitative"
                             :axis {:title "Total Value"}}
                         :color {:field "group"
                                 :type "nominal"}}}]
    (darkstar/vega-lite-spec->svg (charred/write-json-str spec))))

(defn histogram
  "Creates a histogram with binning"
  []
  (let [spec {:$schema "https://vega.github.io/schema/vega-lite/v4.json"
              :width 400
              :height 200
              :data {:values [{:value 1} {:value 2} {:value 2} {:value 3}
                              {:value 3} {:value 3} {:value 4} {:value 4}
                              {:value 4} {:value 4} {:value 5} {:value 5}
                              {:value 5} {:value 5} {:value 5} {:value 6}
                              {:value 6} {:value 6} {:value 7} {:value 7}
                              {:value 8} {:value 8} {:value 8} {:value 9}]}
              :mark "bar"
              :encoding {:x {:bin {:maxbins 10}
                             :field "value"
                             :type "quantitative"}
                         :y {:aggregate "count"
                             :type "quantitative"}}}]
    (darkstar/vega-lite-spec->svg (charred/write-json-str spec))))

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

;; ==================== D3 Examples ====================

(defn d3-bar-chart
  "Create a bar chart using D3.js"
  []
  (let [data [{:name "Apples" :value 28}
              {:name "Bananas" :value 55}
              {:name "Cherries" :value 43}
              {:name "Dates" :value 91}
              {:name "Elderberry" :value 67}]]
    (darkstar/d3-simple-bar-chart data)))

(defn d3-scatter-chart
  "Create a scatter plot using D3.js"
  []
  (let [data [{:x 10 :y 20}
              {:x 25 :y 35}
              {:x 40 :y 55}
              {:x 55 :y 30}
              {:x 70 :y 80}
              {:x 85 :y 45}
              {:x 95 :y 72}]]
    (darkstar/d3-scatter-plot data)))

(defn d3-advanced-bar-chart
  "Create an advanced bar chart with custom D3 script"
  []
  (let [data [{:category "Q1" :sales 1200 :costs 800}
              {:category "Q2" :sales 1400 :costs 900}
              {:category "Q3" :sales 1100 :costs 750}
              {:category "Q4" :sales 1600 :costs 1000}]]
    (darkstar/d3-script->svg
     (str "
        var data = " (charred/write-json-str data) ";
        
        var margin = {top: 30, right: 30, bottom: 40, left: 50};
        var width = 600 - margin.left - margin.right;
        var height = 400 - margin.top - margin.bottom;
        
        // Create SVG
        var svg = global.document.createElementNS('http://www.w3.org/2000/svg', 'svg');
        svg.setAttribute('width', width + margin.left + margin.right);
        svg.setAttribute('height', height + margin.top + margin.bottom);
        global.svgElement = svg;
        
        var g = global.document.createElementNS('http://www.w3.org/2000/svg', 'g');
        g.setAttribute('transform', 'translate(' + margin.left + ',' + margin.top + ')');
        svg.appendChild(g);
        
        // Title
        var title = global.document.createElementNS('http://www.w3.org/2000/svg', 'text');
        title.setAttribute('x', width / 2);
        title.setAttribute('y', -10);
        title.setAttribute('text-anchor', 'middle');
        title.setAttribute('font-family', 'Arial, sans-serif');
        title.setAttribute('font-size', '16px');
        title.setAttribute('font-weight', 'bold');
        title.textContent = 'Quarterly Sales vs Costs';
        g.appendChild(title);
        
        // Scales
        var x0 = d3.scaleBand().range([0, width]).domain(data.map(d => d.category)).padding(0.1);
        var x1 = d3.scaleBand().domain(['sales', 'costs']).range([0, x0.bandwidth()]).padding(0.05);
        var y = d3.scaleLinear().range([height, 0]).domain([0, d3.max(data, d => Math.max(d.sales, d.costs))]);
        
        // Colors
        var colors = {sales: '#4CAF50', costs: '#FF5722'};
        
        // Create grouped bars
        data.forEach(function(d) {
          // Sales bar
          var salesRect = global.document.createElementNS('http://www.w3.org/2000/svg', 'rect');
          salesRect.setAttribute('x', x0(d.category) + x1('sales'));
          salesRect.setAttribute('y', y(d.sales));
          salesRect.setAttribute('width', x1.bandwidth());
          salesRect.setAttribute('height', height - y(d.sales));
          salesRect.setAttribute('fill', colors.sales);
          g.appendChild(salesRect);
          
          // Costs bar
          var costsRect = global.document.createElementNS('http://www.w3.org/2000/svg', 'rect');
          costsRect.setAttribute('x', x0(d.category) + x1('costs'));
          costsRect.setAttribute('y', y(d.costs));
          costsRect.setAttribute('width', x1.bandwidth());
          costsRect.setAttribute('height', height - y(d.costs));
          costsRect.setAttribute('fill', colors.costs);
          g.appendChild(costsRect);
          
          // Category label
          var label = global.document.createElementNS('http://www.w3.org/2000/svg', 'text');
          label.setAttribute('x', x0(d.category) + x0.bandwidth() / 2);
          label.setAttribute('y', height + 20);
          label.setAttribute('text-anchor', 'middle');
          label.setAttribute('font-family', 'Arial, sans-serif');
          label.setAttribute('font-size', '12px');
          label.textContent = d.category;
          g.appendChild(label);
        });
        
        // Legend
        var legend = global.document.createElementNS('http://www.w3.org/2000/svg', 'g');
        legend.setAttribute('transform', 'translate(' + (width - 100) + ',20)');
        
        // Sales legend
        var salesLegendRect = global.document.createElementNS('http://www.w3.org/2000/svg', 'rect');
        salesLegendRect.setAttribute('x', 0);
        salesLegendRect.setAttribute('y', 0);
        salesLegendRect.setAttribute('width', 15);
        salesLegendRect.setAttribute('height', 15);
        salesLegendRect.setAttribute('fill', colors.sales);
        legend.appendChild(salesLegendRect);
        
        var salesLegendText = global.document.createElementNS('http://www.w3.org/2000/svg', 'text');
        salesLegendText.setAttribute('x', 20);
        salesLegendText.setAttribute('y', 12);
        salesLegendText.setAttribute('font-family', 'Arial, sans-serif');
        salesLegendText.setAttribute('font-size', '12px');
        salesLegendText.textContent = 'Sales';
        legend.appendChild(salesLegendText);
        
        // Costs legend
        var costsLegendRect = global.document.createElementNS('http://www.w3.org/2000/svg', 'rect');
        costsLegendRect.setAttribute('x', 0);
        costsLegendRect.setAttribute('y', 20);
        costsLegendRect.setAttribute('width', 15);
        costsLegendRect.setAttribute('height', 15);
        costsLegendRect.setAttribute('fill', colors.costs);
        legend.appendChild(costsLegendRect);
        
        var costsLegendText = global.document.createElementNS('http://www.w3.org/2000/svg', 'text');
        costsLegendText.setAttribute('x', 20);
        costsLegendText.setAttribute('y', 32);
        costsLegendText.setAttribute('font-family', 'Arial, sans-serif');
        costsLegendText.setAttribute('font-size', '12px');
        costsLegendText.textContent = 'Costs';
        legend.appendChild(costsLegendText);
        
        g.appendChild(legend);
      "))))

(defn generate-all-d3-examples
  "Generate all D3 example charts and save them to files"
  []
  (let [examples [["d3-simple-bar.svg" (d3-bar-chart)]
                  ["d3-scatter.svg" (d3-scatter-chart)]
                  ["d3-advanced-bar.svg" (d3-advanced-bar-chart)]]]
    (doseq [[filename svg] examples]
      (save-example svg filename)
      (println "Generated" filename))
    (println "All D3 examples generated!")))

(defn generate-all-examples-with-d3
  "Generate all examples including both Vega/Vega-lite and D3 examples"
  []
  (generate-all-examples)
  (generate-all-d3-examples))

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