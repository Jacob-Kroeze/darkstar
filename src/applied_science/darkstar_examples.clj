(ns applied-science.darkstar-examples
  "Working examples for the Darkstar library demonstrating Vega, Vega-lite, D3.js, and Observable Plot rendering to SVG.
   
   This namespace provides comprehensive examples of server-side data visualization using:
   - Vega: Grammar of graphics declarative visualizations
   - Vega-lite: High-level grammar for statistical graphics  
   - D3.js: Low-level imperative DOM manipulation for custom charts
   - Observable Plot: Modern grammar of graphics for exploratory data analysis
   
   All examples generate SVG output that can be saved to files or embedded in web pages."
  (:require [applied-science.darkstar :as darkstar]
            [charred.api :as charred]
            [clojure.java.io :as io]))

;; ============================================================================
;; Vega Examples (Low-level grammar of graphics)
;; ============================================================================

(defn vega-bar-chart
  "Basic bar chart using pure Vega specification"
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

;; ============================================================================
;; Vega-Lite Examples (High-level statistical graphics)
;; ============================================================================

(defn vega-lite-bar-chart
  "Simple bar chart using Vega-lite specification"
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

(defn vega-lite-scatter-plot
  "Scatter plot with color encoding using Vega-lite"
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

(defn vega-lite-line-chart
  "Multi-series line chart using Vega-lite"
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

(defn vega-lite-heatmap
  "Heatmap visualization using Vega-lite"
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

(defn vega-lite-aggregated-bar-chart
  "Bar chart with data aggregation using Vega-lite"
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

(defn vega-lite-histogram
  "Histogram with binning using Vega-lite"
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
;; D3 Examples (Imperative DOM manipulation)
;; ============================================================================

(defn d3-bar-chart
  "Simple bar chart using D3.js"
  []
  (let [data [{:name "Apples" :value 28}
              {:name "Bananas" :value 55}
              {:name "Cherries" :value 43}
              {:name "Dates" :value 91}
              {:name "Elderberry" :value 67}]]
    (darkstar/d3-simple-bar-chart data)))

(defn d3-scatter-plot
  "Scatter plot using D3.js"
  []
  (let [data [{:x 10 :y 20}
              {:x 25 :y 35}
              {:x 40 :y 55}
              {:x 55 :y 30}
              {:x 70 :y 80}
              {:x 85 :y 45}
              {:x 95 :y 72}]]
    (darkstar/d3-scatter-plot data)))

(defn d3-grouped-bar-chart
  "Grouped bar chart with custom D3 script"
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
        
        // Use real D3 scales
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
        });
      "))))

(defn d3-advanced-features
  "Advanced D3 features: data joins, selections, scales, and axes"
  []
  (let [data [{:category "Q1" :sales 1200 :costs 800}
              {:category "Q2" :sales 1400 :costs 900}
              {:category "Q3" :sales 1100 :costs 750}
              {:category "Q4" :sales 1600 :costs 1000}]]
    (darkstar/d3-script->svg
     (str "
        var data = " (charred/write-json-str data) ";
        
        var margin = {top: 30, right: 80, bottom: 50, left: 60};
        var width = 600 - margin.left - margin.right;
        var height = 400 - margin.top - margin.bottom;
        
        // Create SVG using real D3 selection API
        var svg = d3.select(global.document.body)
          .append('svg')
          .attr('width', width + margin.left + margin.right)
          .attr('height', height + margin.top + margin.bottom);
        
        var g = svg.append('g')
          .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');
        
        // Use real D3 scales
        var x0 = d3.scaleBand()
          .domain(data.map(d => d.category))
          .range([0, width])
          .padding(0.1);
        
        var x1 = d3.scaleBand()
          .domain(['sales', 'costs'])
          .range([0, x0.bandwidth()])
          .padding(0.05);
        
        var y = d3.scaleLinear()
          .domain([0, d3.max(data, d => Math.max(d.sales, d.costs))])
          .nice()
          .range([height, 0]);
        
        // Use real D3 color scale
        var color = d3.scaleOrdinal()
          .domain(['sales', 'costs'])
          .range(['#2E86AB', '#A23B72']);
        
        // Create real D3 axes
        var xAxis = d3.axisBottom(x0);
        var yAxis = d3.axisLeft(y);
        
        // Add axes using D3 call
        g.append('g')
          .attr('class', 'x-axis')
          .attr('transform', 'translate(0,' + height + ')')
          .call(xAxis);
        
        g.append('g')
          .attr('class', 'y-axis')
          .call(yAxis);
        
        // Use D3 data joins for bars
        var categories = g.selectAll('.category')
          .data(data)
          .enter().append('g')
          .attr('class', 'category')
          .attr('transform', d => 'translate(' + x0(d.category) + ',0)');
        
        // Sales and costs bars using D3 selections
        categories.append('rect')
          .attr('class', 'sales-bar')
          .attr('x', x1('sales'))
          .attr('width', x1.bandwidth())
          .attr('y', d => y(d.sales))
          .attr('height', d => height - y(d.sales))
          .attr('fill', color('sales'));
        
        categories.append('rect')
          .attr('class', 'costs-bar')
          .attr('x', x1('costs'))
          .attr('width', x1.bandwidth())
          .attr('y', d => y(d.costs))
          .attr('height', d => height - y(d.costs))
          .attr('fill', color('costs'));
        
        // Add title and legend using D3 selections
        svg.append('text')
          .attr('x', (width + margin.left + margin.right) / 2)
          .attr('y', 20)
          .attr('text-anchor', 'middle')
          .attr('font-family', 'Arial, sans-serif')
          .attr('font-size', '16px')
          .attr('font-weight', 'bold')
          .attr('fill', '#333')
          .text('Real D3.js Features Demo');
        
        // Store the SVG for extraction
        global.svgElement = svg.node();
      "))))

;; ============================================================================
;; Helper Functions
;; ============================================================================

(defn save-example
  "Saves an SVG string to a file in the examples directory"
  [svg-string filename]
  (spit (str "examples/" filename) svg-string)
  (println (str "Saved to examples/" filename)))

;; ============================================================================
;; Generation Functions
;; ============================================================================

(defn generate-vega-examples
  "Generate all Vega example visualizations"
  []
  (io/make-parents "examples/dummy.txt")
  (println "Generating Vega examples...")
  (save-example (vega-bar-chart) "vega-bar.svg")
  (println "Vega examples generated!"))

(defn generate-vega-lite-examples
  "Generate all Vega-lite example visualizations"
  []
  (io/make-parents "examples/dummy.txt")
  (println "Generating Vega-lite examples...")
  (save-example (vega-lite-bar-chart) "vega-lite-bar.svg")
  (save-example (vega-lite-scatter-plot) "vega-lite-scatter.svg")
  (save-example (vega-lite-line-chart) "vega-lite-line.svg")
  (save-example (vega-lite-heatmap) "vega-lite-heatmap.svg")
  (save-example (vega-lite-aggregated-bar-chart) "vega-lite-aggregated-bar.svg")
  (save-example (vega-lite-histogram) "vega-lite-histogram.svg")
  (println "Vega-lite examples generated!"))

(defn generate-d3-examples
  "Generate all D3 example visualizations"
  []
  (io/make-parents "examples/dummy.txt")
  (println "Generating D3 examples...")
  (save-example (d3-bar-chart) "d3-bar.svg")
  (save-example (d3-scatter-plot) "d3-scatter.svg")
  (save-example (d3-grouped-bar-chart) "d3-grouped-bar.svg")
  (save-example (d3-advanced-features) "d3-advanced-features.svg")
  (println "D3 examples generated!"))

;; ============================================================================
;; Observable Plot Examples (Modern grammar of graphics)
;; ============================================================================

(defn plot-bar-chart
  "Bar chart using Observable Plot"
  []
  (darkstar/plot-bar-chart
   [{:name "A" :value 28}
    {:name "B" :value 55}
    {:name "C" :value 43}
    {:name "D" :value 91}
    {:name "E" :value 81}
    {:name "F" :value 53}]))

(defn plot-scatter-plot
  "Scatter plot using Observable Plot"
  []
  (darkstar/plot-scatter-plot
   [{:x 1 :y 2}
    {:x 2 :y 4}
    {:x 3 :y 3}
    {:x 4 :y 5}
    {:x 5 :y 4}
    {:x 6 :y 7}
    {:x 7 :y 6}
    {:x 8 :y 9}]))

(defn plot-line-chart
  "Line chart using Observable Plot"
  []
  (darkstar/plot-line-chart
   [{:x 1 :y 2}
    {:x 2 :y 4}
    {:x 3 :y 3}
    {:x 4 :y 5}
    {:x 5 :y 4}
    {:x 6 :y 7}
    {:x 7 :y 6}
    {:x 8 :y 9}]))

(defn plot-area-chart
  "Area chart using Observable Plot"
  []
  (darkstar/plot-area-chart
   [{:x 1 :y 2}
    {:x 2 :y 4}
    {:x 3 :y 3}
    {:x 4 :y 5}
    {:x 5 :y 4}
    {:x 6 :y 7}
    {:x 7 :y 6}
    {:x 8 :y 9}]))

(defn plot-histogram
  "Histogram using Observable Plot"
  []
  (darkstar/plot-histogram
   [{:value 1.2}
    {:value 2.4}
    {:value 1.8}
    {:value 3.1}
    {:value 2.7}
    {:value 1.9}
    {:value 2.2}
    {:value 3.5}
    {:value 1.1}
    {:value 2.8}]))

(defn plot-custom-chart
  "Custom Observable Plot with multiple marks"
  []
  (darkstar/plot-script->svg
   "
   var data = [
     {x: 1, y: 2, category: 'A'},
     {x: 2, y: 4, category: 'B'},
     {x: 3, y: 3, category: 'A'},
     {x: 4, y: 5, category: 'B'}
   ];
   
   var plot = Plot.plot({
     marks: [
       Plot.frame(),
       Plot.dot(data, {x: 'x', y: 'y', fill: 'category', r: 5})
     ],
     width: 500,
     height: 300,
     marginLeft: 50
   });
   
   global.plotElement = plot;
   "))

(defn plot-facet-chart
  "Observable Plot faceted chart example"
  []
  (darkstar/plot-script->svg
   "
   var data = [
     {x: 1, y: 2, category: 'A', type: 'small'},
     {x: 2, y: 4, category: 'B', type: 'small'},
     {x: 3, y: 3, category: 'A', type: 'large'},
     {x: 4, y: 5, category: 'B', type: 'large'},
     {x: 5, y: 1, category: 'A', type: 'small'},
     {x: 6, y: 6, category: 'B', type: 'large'}
   ];
   
   var plot = Plot.plot({
     marks: [
       Plot.dot(data, {x: 'x', y: 'y', fill: 'category', fx: 'type'})
     ],
     width: 600,
     height: 300,
     marginLeft: 50
   });
   
   global.plotElement = plot;
   "))

(defn plot-smooth-line
  "Observable Plot with smooth line"
  []
  (darkstar/plot-script->svg
   "
   var data = [
     {x: 1, y: 2}, {x: 2, y: 5}, {x: 3, y: 3}, 
     {x: 4, y: 8}, {x: 5, y: 4}, {x: 6, y: 7},
     {x: 7, y: 6}, {x: 8, y: 9}
   ];
   
   var plot = Plot.plot({
     marks: [
       Plot.dot(data, {x: 'x', y: 'y', fill: 'steelblue'}),
       Plot.line(data, Plot.windowY(3, {x: 'x', y: 'y', stroke: 'red'}))
     ],
     width: 500,
     height: 300,
     marginLeft: 50
   });
   
   global.plotElement = plot;
   "))

(defn generate-plot-examples
  "Generate all Observable Plot example visualizations"
  []
  (io/make-parents "examples/dummy.txt")
  (println "Generating Observable Plot examples...")
  (save-example (plot-bar-chart) "plot-bar.svg")
  (save-example (plot-scatter-plot) "plot-scatter.svg")
  (save-example (plot-line-chart) "plot-line.svg")
  (save-example (plot-area-chart) "plot-area.svg")
  (save-example (plot-histogram) "plot-histogram.svg")
  (save-example (plot-custom-chart) "plot-custom.svg")
  (save-example (plot-facet-chart) "plot-facet.svg")
  (save-example (plot-smooth-line) "plot-smooth.svg")
  (println "Observable Plot examples generated!"))

(defn generate-all-examples
  "Generate all example visualizations (Vega, Vega-lite, D3, and Observable Plot)"
  []
  (generate-vega-examples)
  (generate-vega-lite-examples)
  (generate-d3-examples)
  (generate-plot-examples))

;; ============================================================================
;; Backwards Compatibility Aliases
;; ============================================================================

;; Keep old function names for backwards compatibility
(def simple-bar-chart vega-bar-chart)
(def simple-vega-lite-bar vega-lite-bar-chart)
(def scatter-plot vega-lite-scatter-plot)
(def line-chart vega-lite-line-chart)
(def heatmap vega-lite-heatmap)
(def aggregated-bar-chart vega-lite-aggregated-bar-chart)
(def histogram vega-lite-histogram)
(def d3-advanced-bar-chart d3-grouped-bar-chart)
(def d3-real-features-demo d3-advanced-features)
(def generate-all-d3-examples generate-d3-examples)
(def generate-all-examples-with-d3 generate-all-examples)

;; ============================================================================
;; Usage Examples and Documentation
;; ============================================================================

(comment
  ;; === Vega Examples (Grammar of Graphics) ===
  (def vega-chart (vega-bar-chart))
  (spit "vega-chart.svg" vega-chart)

  ;; === Vega-Lite Examples (High-level Statistical Graphics) ===
  (def vl-scatter (vega-lite-scatter-plot))
  (def vl-line (vega-lite-line-chart))
  (def vl-heatmap (vega-lite-heatmap))

  ;; === D3 Examples (Imperative DOM Manipulation) ===
  (def d3-chart (d3-bar-chart))
  (def d3-advanced (d3-advanced-features))

  ;; === Generate All Examples by Type ===
  (generate-vega-examples)
  (generate-vega-lite-examples)
  (generate-d3-examples)

  ;; === Generate All Examples at Once ===
  (generate-all-examples)

  ;; === Working with File Paths ===
  (binding [darkstar/*base-directory* "/path/to/data/"]
    (darkstar/vega-spec->svg spec-with-relative-paths))

  ;; === Convert Your Own Specifications ===
  ;; Vega-lite
  (-> (slurp "my-spec.vl.json")
      darkstar/vega-lite-spec->svg
      (spit "my-output.svg"))

  ;; Vega
  (-> (slurp "my-spec.vg.json")
      darkstar/vega-spec->svg
      (spit "my-output.svg"))

  ;; D3 with custom data
  (-> [{:x 1 :y 2} {:x 3 :y 4}]
      examples/d3-scatter-plot
      (spit "my-d3-chart.svg")))
