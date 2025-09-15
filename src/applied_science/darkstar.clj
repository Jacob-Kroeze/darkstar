(ns applied-science.darkstar
  (:require [charred.api :as charred]))

(def ^:dynamic *base-directory* nil)

(defn read-file
  "A very, very slight polyfill for Node's fs.readFile that uses `*base-directory*` as Vega's idea of current working directory."
  [filename]
  ;; TODO only system error handling!
  (slurp (.getAbsolutePath (java.io.File. (str *base-directory* filename)))))

(def engine
  (let [context (-> (org.graalvm.polyglot.Context/newBuilder (into-array String ["js"]))
                    (.allowAllAccess true)
                    (.build))]
    ;; Get the JavaScript bindings
    (let [js (.getBindings context "js")]
      ;; Add polyfills and libraries
      (.eval context "js" "
async function fetch(path, options) {
  var body = Java.type('clojure.core$slurp').invokeStatic(path,null);
  return {'ok' : true,
          'body' : body,
          'text' : (function() {return body;}),
          'json' : (function() {return JSON.parse(body);})};
}
function readFile(path, callback) {
  try {
    var data = Java.type('applied_science.darkstar$read_file').invokeStatic(path);
    callback(null, data);
  } catch (err) {
    printErr(err);
  }
}
var fs = {'readFile':readFile};
")
      (.eval context "js" (slurp (clojure.java.io/resource "vega.js")))
      (.eval context "js" (slurp (clojure.java.io/resource "vega-lite.js"))))
    context))

(defn make-js-fn [js-text]
  ;; Wrap the function text in parentheses for proper evaluation
  (let [wrapped-text (str "(" js-text ")")
        ^org.graalvm.polyglot.Value f (.eval engine "js" wrapped-text)]
    (fn [& args]
      (let [result (.execute f (to-array args))]
        ;; If the result is a GraalJS Value, try to convert it to Java object
        (if (.hasArrayElements result)
          (.as result (Class/forName "[Ljava.lang.Object;"))
          (if (.canExecute result)
            result
            (.as result Object)))))))

(def vega-lite->vega
  "Converts a VegaLite spec into a Vega spec."
  (make-js-fn "function(vlSpec) { return JSON.stringify(vegaLite.compile(JSON.parse(vlSpec)).spec);}"))

(def vega-spec->view
  "Converts a Vega spec into a Vega view object, finalizing all resources."
  (make-js-fn "function(spec) { return new vega.View(vega.parse(JSON.parse(spec)), {renderer:'svg'}).finalize();}"))

(def view->svg
  "Converts a Vega view object into an SVG."
  (make-js-fn "function (view) {
    var promise = Java.type('clojure.core$promise').invokeStatic();
    view.toSVG(1.0).then(function(svg) {
        Java.type('clojure.core$deliver').invokeStatic(promise, svg);
    }).catch(function(err) {
        Java.type('clojure.core$deliver').invokeStatic(promise, '<svg><text>Error: ' + err + '</text></svg>');
    });
    return promise;
}"))

(defn vega-spec->svg
  "Calls Vega to render the spec in `vega-spec-json-string` to the SVG described by that spec."
  [vega-spec-json-string]
  @(view->svg (vega-spec->view vega-spec-json-string)))

(defn vega-lite-spec->svg
  "Converts `vega-lite-spec-json-string` to a full Vega spec, then uses Vega to render the SVG described by that spec."
  [vega-lite-spec-json-string]
  (vega-spec->svg (vega-lite->vega vega-lite-spec-json-string)))

;; ==================== D3 Integration ====================

(def d3-engine
  "Separate engine for D3.js with enhanced DOM polyfills"
  (let [context (-> (org.graalvm.polyglot.Context/newBuilder (into-array String ["js"]))
                    (.allowAllAccess true)
                    (.build))]
    ;; Add enhanced DOM polyfills for real D3
    (.eval context "js" "
// Enhanced DOM polyfills for real D3.js
global = globalThis;

function createSVGElement(tagName) {
  var element = {
    tagName: tagName.toUpperCase(),
    nodeName: tagName.toUpperCase(),
    nodeType: 1, // ELEMENT_NODE
    attributes: {},
    style: {},
    children: [],
    childNodes: [],
    textContent: '',
    innerHTML: '',
    className: '',
    id: '',
    
    setAttribute: function(name, value) {
      this.attributes[name] = String(value);
      if (name === 'class') this.className = String(value);
      if (name === 'id') this.id = String(value);
    },
    
    getAttribute: function(name) {
      return this.attributes[name] || null;
    },
    
    hasAttribute: function(name) {
      return this.attributes.hasOwnProperty(name);
    },
    
    removeAttribute: function(name) {
      delete this.attributes[name];
      if (name === 'class') this.className = '';
      if (name === 'id') this.id = '';
    },
    
    appendChild: function(child) {
      if (child && child !== this) {
        child.parentNode = this;
        this.children.push(child);
        this.childNodes.push(child);
      }
      return child;
    },
    
    insertBefore: function(newChild, referenceChild) {
      if (newChild && newChild !== this) {
        newChild.parentNode = this;
        if (referenceChild) {
          var index = this.children.indexOf(referenceChild);
          if (index !== -1) {
            this.children.splice(index, 0, newChild);
            this.childNodes.splice(index, 0, newChild);
          } else {
            this.children.push(newChild);
            this.childNodes.push(newChild);
          }
        } else {
          this.children.push(newChild);
          this.childNodes.push(newChild);
        }
      }
      return newChild;
    },
    
    removeChild: function(child) {
      var index = this.children.indexOf(child);
      if (index !== -1) {
        this.children.splice(index, 1);
        this.childNodes.splice(index, 1);
        child.parentNode = null;
      }
      return child;
    },
    
    querySelector: function(selector) {
      // Simple selector support
      if (selector.startsWith('#')) {
        var id = selector.substring(1);
        return this.getElementById(id);
      } else if (selector.startsWith('.')) {
        var className = selector.substring(1);
        for (var i = 0; i < this.children.length; i++) {
          if (this.children[i].className === className) {
            return this.children[i];
          }
        }
      }
      return null;
    },
    
    querySelectorAll: function(selector) {
      var results = [];
      if (selector.startsWith('.')) {
        var className = selector.substring(1);
        for (var i = 0; i < this.children.length; i++) {
          if (this.children[i].className === className) {
            results.push(this.children[i]);
          }
        }
      }
      return results;
    },
    
    getElementById: function(id) {
      if (this.id === id) return this;
      for (var i = 0; i < this.children.length; i++) {
        var result = this.children[i].getElementById(id);
        if (result) return result;
      }
      return null;
    },
    
    getElementsByTagName: function(tagName) {
      var results = [];
      if (this.tagName.toLowerCase() === tagName.toLowerCase()) {
        results.push(this);
      }
      for (var i = 0; i < this.children.length; i++) {
        results = results.concat(this.children[i].getElementsByTagName(tagName));
      }
      return results;
    },
    
    addEventListener: function() {},
    removeEventListener: function() {},
    dispatchEvent: function() { return true; },
    
    // Custom toSVG method for output
    toSVG: function() {
      var svg = '<' + this.tagName.toLowerCase();
      
      // Add attributes
      for (var attr in this.attributes) {
        svg += ' ' + attr + '=\"' + this.attributes[attr] + '\"';
      }
      
      // Add style
      var styleStr = '';
      for (var prop in this.style) {
        if (this.style[prop]) {
          styleStr += prop + ':' + this.style[prop] + ';';
        }
      }
      if (styleStr) {
        svg += ' style=\"' + styleStr + '\"';
      }
      
      svg += '>';
      
      // Add text content
      if (this.textContent) {
        svg += this.textContent;
      }
      
      // Add children
      for (var i = 0; i < this.children.length; i++) {
        svg += this.children[i].toSVG();
      }
      
      svg += '</' + this.tagName.toLowerCase() + '>';
      return svg;
    }
  };
  
  return element;
}

// Create document object
global.document = {
  documentElement: null,
  body: null,
  
  createElement: function(tagName) {
    var el = createSVGElement(tagName);
    el.ownerDocument = this;
    return el;
  },
  
  createElementNS: function(namespace, tagName) {
    var el = createSVGElement(tagName);
    el.ownerDocument = this;
    return el;
  },
  
  querySelector: function(selector) {
    return null;
  },
  
  querySelectorAll: function(selector) {
    return [];
  },
  
  addEventListener: function() {},
  removeEventListener: function() {},
  dispatchEvent: function() { return true; }
};

// Create body and documentElement
global.document.body = global.document.createElement('body');
global.document.documentElement = global.document.createElement('html');
global.document.documentElement.appendChild(global.document.body);

// Window-like object for D3
global.window = global;
global.navigator = { userAgent: 'GraalJS' };

// Basic event support
global.Event = function(type) { this.type = type; };
global.CustomEvent = global.Event;
")
    ;; Load real D3.js
    (.eval context "js" (slurp (clojure.java.io/resource "d3.js")))
    context))

(defn make-d3-fn [js-text]
  "Create a D3 function that returns SVG string"
  (let [wrapped-text (str "(" js-text ")")
        ^org.graalvm.polyglot.Value f (.eval d3-engine "js" wrapped-text)]
    (fn [& args]
      (let [result (.execute f (to-array args))]
        (if (.hasArrayElements result)
          (.as result (Class/forName "[Ljava.lang.Object;"))
          (if (.canExecute result)
            result
            (.as result Object)))))))

(defn- clj->js-json
  "Convert Clojure data to JavaScript JSON string using Charred"
  [data]
  (charred/write-json-str data))

(defn d3-script->svg
  "Execute D3 JavaScript code and return SVG string.
   The script should create an SVG element and return its HTML."
  [d3-script]
  (let [wrapped-script (str "
    (function() {
      " d3-script "
      
      // Find the SVG element and return its HTML
      var svg = global.document.querySelector ? global.document.querySelector('svg') : null;
      if (!svg && global.svgElement) {
        svg = global.svgElement;
      }
      return svg ? svg.toSVG() : '<svg></svg>';
    })()
  ")]
    (.as (.eval d3-engine "js" wrapped-script) String)))

;; D3 Example Functions

(defn d3-simple-bar-chart
  "Create a simple bar chart using D3"
  [data]
  (d3-script->svg
   (str "
      var data = " (clj->js-json data) ";
      
      var margin = {top: 20, right: 20, bottom: 30, left: 40};
      var width = 500 - margin.left - margin.right;
      var height = 300 - margin.top - margin.bottom;
      
      // Create SVG
      var svg = global.document.createElementNS('http://www.w3.org/2000/svg', 'svg');
      svg.setAttribute('width', width + margin.left + margin.right);
      svg.setAttribute('height', height + margin.top + margin.bottom);
      global.svgElement = svg;
      
      // Create group for margins
      var g = global.document.createElementNS('http://www.w3.org/2000/svg', 'g');
      g.setAttribute('transform', 'translate(' + margin.left + ',' + margin.top + ')');
      svg.appendChild(g);
      
      // Set up scales
      var x = d3.scaleBand()
          .range([0, width])
          .domain(data.map(function(d) { return d.name; }))
          .padding(0.1);
      
      var y = d3.scaleLinear()
          .range([height, 0])
          .domain([0, d3.max(data, function(d) { return d.value; })]);
      
      // Create bars
      data.forEach(function(d, i) {
        var rect = global.document.createElementNS('http://www.w3.org/2000/svg', 'rect');
        rect.setAttribute('x', x(d.name));
        rect.setAttribute('width', x.bandwidth());
        rect.setAttribute('y', y(d.value));
        rect.setAttribute('height', height - y(d.value));
        rect.setAttribute('fill', 'steelblue');
        g.appendChild(rect);
        
        // Add text label
        var text = global.document.createElementNS('http://www.w3.org/2000/svg', 'text');
        text.setAttribute('x', x(d.name) + x.bandwidth() / 2);
        text.setAttribute('y', y(d.value) - 5);
        text.setAttribute('text-anchor', 'middle');
        text.setAttribute('font-family', 'Arial, sans-serif');
        text.setAttribute('font-size', '12px');
        text.textContent = d.value;
        g.appendChild(text);
      });
      
      // Add X axis
      var xAxis = global.document.createElementNS('http://www.w3.org/2000/svg', 'g');
      xAxis.setAttribute('transform', 'translate(0,' + height + ')');
      data.forEach(function(d) {
        var text = global.document.createElementNS('http://www.w3.org/2000/svg', 'text');
        text.setAttribute('x', x(d.name) + x.bandwidth() / 2);
        text.setAttribute('y', 15);
        text.setAttribute('text-anchor', 'middle');
        text.setAttribute('font-family', 'Arial, sans-serif');
        text.setAttribute('font-size', '12px');
        text.textContent = d.name;
        xAxis.appendChild(text);
      });
      g.appendChild(xAxis);
    ")))

(defn d3-scatter-plot
  "Create a scatter plot using D3"
  [data]
  (d3-script->svg
   (str "
      var data = " (clj->js-json data) ";
      
      var margin = {top: 20, right: 20, bottom: 30, left: 40};
      var width = 500 - margin.left - margin.right;
      var height = 300 - margin.top - margin.bottom;
      
      // Create SVG
      var svg = global.document.createElementNS('http://www.w3.org/2000/svg', 'svg');
      svg.setAttribute('width', width + margin.left + margin.right);
      svg.setAttribute('height', height + margin.top + margin.bottom);
      global.svgElement = svg;
      
      var g = global.document.createElementNS('http://www.w3.org/2000/svg', 'g');
      g.setAttribute('transform', 'translate(' + margin.left + ',' + margin.top + ')');
      svg.appendChild(g);
      
      // Set up scales
      var x = d3.scaleLinear()
          .range([0, width])
          .domain([0, d3.max(data, function(d) { return d.x; })]);
      
      var y = d3.scaleLinear()
          .range([height, 0])
          .domain([0, d3.max(data, function(d) { return d.y; })]);
      
      // Create dots
      data.forEach(function(d) {
        var circle = global.document.createElementNS('http://www.w3.org/2000/svg', 'circle');
        circle.setAttribute('cx', x(d.x));
        circle.setAttribute('cy', y(d.y));
        circle.setAttribute('r', 4);
        circle.setAttribute('fill', 'steelblue');
        circle.setAttribute('opacity', 0.7);
        g.appendChild(circle);
      });
    ")))

;; ==================== Observable Plot Integration ====================

(def plot-engine
  "Separate engine for Observable Plot with D3 and Plot libraries"
  (let [context (-> (org.graalvm.polyglot.Context/newBuilder (into-array String ["js"]))
                    (.allowAllAccess true)
                    (.build))]
    ;; Add DOM polyfills (same as D3 but optimized for Plot)
    (.eval context "js" "
// DOM polyfills for Observable Plot
global = globalThis;

function createSVGElement(tagName) {
  var element = {
    tagName: tagName.toUpperCase(),
    nodeName: tagName.toUpperCase(),
    nodeType: 1,
    attributes: {},
    style: {},
    children: [],
    childNodes: [],
    textContent: '',
    innerHTML: '',
    className: '',
    id: '',
    
    setAttribute: function(name, value) {
      this.attributes[name] = String(value);
      if (name === 'class') this.className = String(value);
      if (name === 'id') this.id = String(value);
    },
    
    getAttribute: function(name) {
      return this.attributes[name] || null;
    },
    
    hasAttribute: function(name) {
      return this.attributes.hasOwnProperty(name);
    },
    
    removeAttribute: function(name) {
      delete this.attributes[name];
      if (name === 'class') this.className = '';
      if (name === 'id') this.id = '';
    },
    
    appendChild: function(child) {
      if (child && child !== this) {
        child.parentNode = this;
        this.children.push(child);
        this.childNodes.push(child);
      }
      return child;
    },
    
    insertBefore: function(newChild, referenceChild) {
      if (newChild && newChild !== this) {
        newChild.parentNode = this;
        if (referenceChild) {
          var index = this.children.indexOf(referenceChild);
          if (index !== -1) {
            this.children.splice(index, 0, newChild);
            this.childNodes.splice(index, 0, newChild);
          } else {
            this.children.push(newChild);
            this.childNodes.push(newChild);
          }
        } else {
          this.children.push(newChild);
          this.childNodes.push(newChild);
        }
      }
      return newChild;
    },
    
    removeChild: function(child) {
      var index = this.children.indexOf(child);
      if (index !== -1) {
        this.children.splice(index, 1);
        this.childNodes.splice(index, 1);
        child.parentNode = null;
      }
      return child;
    },
    
    querySelector: function(selector) {
      if (selector.startsWith('#')) {
        var id = selector.substring(1);
        return this.getElementById(id);
      } else if (selector.startsWith('.')) {
        var className = selector.substring(1);
        for (var i = 0; i < this.children.length; i++) {
          if (this.children[i].className === className) {
            return this.children[i];
          }
        }
      }
      return null;
    },
    
    querySelectorAll: function(selector) {
      var results = [];
      if (selector.startsWith('.')) {
        var className = selector.substring(1);
        for (var i = 0; i < this.children.length; i++) {
          if (this.children[i].className === className) {
            results.push(this.children[i]);
          }
        }
      }
      return results;
    },
    
    getElementById: function(id) {
      if (this.id === id) return this;
      for (var i = 0; i < this.children.length; i++) {
        var result = this.children[i].getElementById(id);
        if (result) return result;
      }
      return null;
    },
    
    getElementsByTagName: function(tagName) {
      var results = [];
      if (this.tagName.toLowerCase() === tagName.toLowerCase()) {
        results.push(this);
      }
      for (var i = 0; i < this.children.length; i++) {
        results = results.concat(this.children[i].getElementsByTagName(tagName));
      }
      return results;
    },
    
    addEventListener: function() {},
    removeEventListener: function() {},
    dispatchEvent: function() { return true; },
    
    toSVG: function() {
      var svg = '<' + this.tagName.toLowerCase();
      
      for (var attr in this.attributes) {
        svg += ' ' + attr + '=\"' + this.attributes[attr] + '\"';
      }
      
      var styleStr = '';
      for (var prop in this.style) {
        if (this.style[prop]) {
          styleStr += prop + ':' + this.style[prop] + ';';
        }
      }
      if (styleStr) {
        svg += ' style=\"' + styleStr + '\"';
      }
      
      svg += '>';
      
      if (this.textContent) {
        svg += this.textContent;
      }
      
      for (var i = 0; i < this.children.length; i++) {
        svg += this.children[i].toSVG();
      }
      
      svg += '</' + this.tagName.toLowerCase() + '>';
      return svg;
    }
  };
  
  return element;
}

// Create document object
global.document = {
  documentElement: null,
  body: null,
  
  createElement: function(tagName) {
    var el = createSVGElement(tagName);
    el.ownerDocument = this;
    return el;
  },
  
  createElementNS: function(namespace, tagName) {
    var el = createSVGElement(tagName);
    el.ownerDocument = this;
    return el;
  },
  
  querySelector: function(selector) {
    return null;
  },
  
  querySelectorAll: function(selector) {
    return [];
  },
  
  addEventListener: function() {},
  removeEventListener: function() {},
  dispatchEvent: function() { return true; }
};

global.document.body = global.document.createElement('body');
global.document.documentElement = global.document.createElement('html');
global.document.documentElement.appendChild(global.document.body);

global.window = global;
global.navigator = { userAgent: 'GraalJS' };

global.Event = function(type) { this.type = type; };
global.CustomEvent = global.Event;
")
    ;; Load D3.js and Observable Plot
    (.eval context "js" (slurp (clojure.java.io/resource "d3.js")))
    (.eval context "js" (slurp (clojure.java.io/resource "plot.js")))
    context))

(defn make-plot-fn [js-text]
  "Create an Observable Plot function"
  (let [wrapped-text (str "(" js-text ")")
        ^org.graalvm.polyglot.Value f (.eval plot-engine "js" wrapped-text)]
    (fn [& args]
      (let [result (.execute f (to-array args))]
        (if (.hasArrayElements result)
          (.as result (Class/forName "[Ljava.lang.Object;"))
          (if (.canExecute result)
            result
            (.as result Object)))))))

(defn plot->svg
  "Execute Observable Plot code and return SVG string.
   Takes a Plot specification and returns the rendered SVG.
   Note: This function expects Observable Plot API format with Plot.mark() functions"
  [plot-spec]
  (let [plot-code (str "
    (function() {
      var spec = " (clj->js-json plot-spec) ";
      
      // Create the plot using Observable Plot API
      var plot = Plot.plot(spec);
      
      // The plot is already an SVG element, get its HTML
      return plot.outerHTML || plot.toString();
    })()
  ")]
    (.as (.eval plot-engine "js" plot-code) String)))

(defn plot-script->svg
  "Execute custom Observable Plot JavaScript code and return SVG string.
   The script should use Plot.plot() and return the SVG element."
  [plot-script]
  (let [wrapped-script (str "
    (function() {
      " plot-script "
      
      // Look for the created plot or SVG element
      var plot = global.plotElement || global.document.querySelector('svg');
      if (plot && plot.outerHTML) {
        return plot.outerHTML;
      } else if (plot && plot.toSVG) {
        return plot.toSVG();
      }
      return '<svg></svg>';
    })()
  ")]
    (.as (.eval plot-engine "js" wrapped-script) String)))

;; Observable Plot Example Functions

(defn plot-bar-chart
  "Create a bar chart using Observable Plot"
  [data]
  (plot-script->svg
   (str "
     var data = " (clj->js-json data) ";
     var plot = Plot.plot({
       marks: [Plot.barY(data, {x: 'name', y: 'value'})],
       width: 500,
       height: 300,
       marginLeft: 50
     });
     global.plotElement = plot;
   ")))

(defn plot-scatter-plot
  "Create a scatter plot using Observable Plot"
  [data]
  (plot-script->svg
   (str "
     var data = " (clj->js-json data) ";
     var plot = Plot.plot({
       marks: [Plot.dot(data, {x: 'x', y: 'y'})],
       width: 500,
       height: 300,
       marginLeft: 50
     });
     global.plotElement = plot;
   ")))

(defn plot-line-chart
  "Create a line chart using Observable Plot"
  [data]
  (plot-script->svg
   (str "
     var data = " (clj->js-json data) ";
     var plot = Plot.plot({
       marks: [Plot.line(data, {x: 'x', y: 'y'})],
       width: 500,
       height: 300,
       marginLeft: 50
     });
     global.plotElement = plot;
   ")))

(defn plot-area-chart
  "Create an area chart using Observable Plot"
  [data]
  (plot-script->svg
   (str "
     var data = " (clj->js-json data) ";
     var plot = Plot.plot({
       marks: [Plot.areaY(data, {x: 'x', y: 'y'})],
       width: 500,
       height: 300,
       marginLeft: 50
     });
     global.plotElement = plot;
   ")))

(defn plot-histogram
  "Create a histogram using Observable Plot"
  [data]
  (plot-script->svg
   (str "
     var data = " (clj->js-json data) ";
     var plot = Plot.plot({
       marks: [Plot.rectY(data, Plot.binX({y: 'count'}, {x: 'value'}))],
       width: 500,
       height: 300,
       marginLeft: 50
     });
     global.plotElement = plot;
   ")))

(comment

  (->> (slurp "vega-lite-movies.json")
       vega-lite-spec->svg
       (spit "vl-movies.svg")))
