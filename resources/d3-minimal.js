// Minimal D3 implementation for server-side SVG generation
// This is a simplified version for POC - in production you'd use the full D3 library

var d3 = (function() {
  
  // Selection implementation
  function Selection(nodes, parents) {
    this._nodes = nodes;
    this._parents = parents;
  }
  
  Selection.prototype.select = function(selector) {
    var nodes = [];
    for (var i = 0; i < this._nodes.length; i++) {
      var node = this._nodes[i];
      if (node) {
        var selected = node.querySelector ? node.querySelector(selector) : null;
        nodes.push(selected);
      } else {
        nodes.push(null);
      }
    }
    return new Selection(nodes, this._nodes);
  };
  
  Selection.prototype.selectAll = function(selector) {
    var groups = [];
    for (var i = 0; i < this._nodes.length; i++) {
      var node = this._nodes[i];
      if (node) {
        var selected = node.querySelectorAll ? Array.from(node.querySelectorAll(selector)) : [];
        groups.push(selected);
      } else {
        groups.push([]);
      }
    }
    return new Selection([].concat.apply([], groups), this._nodes);
  };
  
  Selection.prototype.append = function(name) {
    var nodes = [];
    for (var i = 0; i < this._nodes.length; i++) {
      var node = this._nodes[i];
      if (node) {
        var element = node.ownerDocument.createElementNS('http://www.w3.org/2000/svg', name);
        node.appendChild(element);
        nodes.push(element);
      } else {
        nodes.push(null);
      }
    }
    return new Selection(nodes, this._nodes);
  };
  
  Selection.prototype.attr = function(name, value) {
    if (arguments.length < 2) {
      var node = this._nodes[0];
      return node ? node.getAttribute(name) : null;
    }
    
    for (var i = 0; i < this._nodes.length; i++) {
      var node = this._nodes[i];
      if (node) {
        if (typeof value === 'function') {
          node.setAttribute(name, value.call(node, node.__data__, i));
        } else {
          node.setAttribute(name, value);
        }
      }
    }
    return this;
  };
  
  Selection.prototype.style = function(name, value) {
    for (var i = 0; i < this._nodes.length; i++) {
      var node = this._nodes[i];
      if (node && node.style) {
        if (typeof value === 'function') {
          node.style[name] = value.call(node, node.__data__, i);
        } else {
          node.style[name] = value;
        }
      }
    }
    return this;
  };
  
  Selection.prototype.text = function(value) {
    if (arguments.length === 0) {
      var node = this._nodes[0];
      return node ? node.textContent : null;
    }
    
    for (var i = 0; i < this._nodes.length; i++) {
      var node = this._nodes[i];
      if (node) {
        if (typeof value === 'function') {
          node.textContent = value.call(node, node.__data__, i);
        } else {
          node.textContent = value;
        }
      }
    }
    return this;
  };
  
  Selection.prototype.data = function(data) {
    var nodes = [];
    for (var i = 0; i < Math.max(this._nodes.length, data.length); i++) {
      var node = this._nodes[i];
      if (!node && i < data.length) {
        // Create placeholder for enter selection
        node = {};
      }
      if (node && i < data.length) {
        node.__data__ = data[i];
        nodes.push(node);
      }
    }
    
    var selection = new Selection(nodes, this._parents);
    
    // Add enter method
    selection.enter = function() {
      var enterNodes = [];
      for (var j = 0; j < data.length; j++) {
        if (j >= selection._nodes.length || !selection._nodes[j] || !selection._nodes[j].nodeType) {
          enterNodes.push({__data__: data[j], __enter__: true});
        }
      }
      return new Selection(enterNodes, selection._parents);
    };
    
    return selection;
  };
  
  // Simple scales
  function scaleLinear() {
    var domain = [0, 1];
    var range = [0, 1];
    
    function scale(x) {
      var t = (x - domain[0]) / (domain[1] - domain[0]);
      return range[0] + t * (range[1] - range[0]);
    }
    
    scale.domain = function(d) {
      if (!arguments.length) return domain;
      domain = d;
      return scale;
    };
    
    scale.range = function(r) {
      if (!arguments.length) return range;
      range = r;
      return scale;
    };
    
    return scale;
  }
  
  function scaleBand() {
    var domain = [];
    var range = [0, 1];
    var paddingInner = 0;
    var paddingOuter = 0;
    
    function scale(x) {
      var i = domain.indexOf(x);
      if (i < 0) return undefined;
      var step = (range[1] - range[0]) / domain.length;
      return range[0] + i * step + paddingOuter * step;
    }
    
    scale.domain = function(d) {
      if (!arguments.length) return domain;
      domain = d;
      return scale;
    };
    
    scale.range = function(r) {
      if (!arguments.length) return range;
      range = r;
      return scale;
    };
    
    scale.bandwidth = function() {
      var step = (range[1] - range[0]) / domain.length;
      return step * (1 - paddingInner);
    };
    
    scale.padding = function(p) {
      if (!arguments.length) return paddingInner;
      paddingInner = p;
      return scale;
    };
    
    return scale;
  }
  
  // Simple max/min functions
  function max(array, accessor) {
    var i = -1, n = array.length, a, b;
    if (accessor == null) {
      while (++i < n) if ((b = array[i]) != null && b >= b) { a = b; break; }
      while (++i < n) if ((b = array[i]) != null && b > a) a = b;
    } else {
      while (++i < n) if ((b = accessor(array[i], i, array)) != null && b >= b) { a = b; break; }
      while (++i < n) if ((b = accessor(array[i], i, array)) != null && b > a) a = b;
    }
    return a;
  }
  
  function min(array, accessor) {
    var i = -1, n = array.length, a, b;
    if (accessor == null) {
      while (++i < n) if ((b = array[i]) != null && b >= b) { a = b; break; }
      while (++i < n) if ((b = array[i]) != null && b < a) a = b;
    } else {
      while (++i < n) if ((b = accessor(array[i], i, array)) != null && b >= b) { a = b; break; }
      while (++i < n) if ((b = accessor(array[i], i, array)) != null && b < a) a = b;
    }
    return a;
  }
  
  // Main D3 object
  function select(selector) {
    var node = typeof selector === 'string' 
      ? (global.document ? global.document.querySelector(selector) : null)
      : selector;
    return new Selection([node], [null]);
  }
  
  return {
    select: select,
    scaleLinear: scaleLinear,
    scaleBand: scaleBand,
    max: max,
    min: min
  };
})();

// Export for use in the engine
if (typeof module !== 'undefined') module.exports = d3;