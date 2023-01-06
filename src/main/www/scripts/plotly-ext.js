// Create a plotly chart.
// What is selected is saved in the address so that reloads will
// retain the selection and multiple windows can be used without
// cookies overwriting each other.

var lastRows = [];
function loadPlotlyFromCSV(title, filename, chartNum) {
  console.log("Loading csv file " + filename);
  Plotly.d3.csv(filename,
                function(err, rows) {
                  if (err) {
                    alert("Error loading " + filename + ": " + err.statusText);
                    return;
                  }
                  lastRows = rows;
                  makePlotlyChart(title, rows, chartNum);
                });
}

function makePlotlyChart(title, rows, chartNum) {
  var config = getConfig(chartNum);

  console.log("Plotting data");
  var all = {}
  for (var i=0; i<rows.length; i++) {
    // Walk this CSV row to find all values contained in it.
    var row = rows[i];
    var time = row['date']
    for (var key in row) {
      if (key == 'date') continue;
      // Check if this is an internal key.
      if (!row.hasOwnProperty(key)) continue;
      // Have we already seen this series?
      if (!(key in all)) {
        // Nope, add a new series
        var visible = 'legendonly';
	var yaxis = 'y1';
        if (key in config['y1']) {
	  yaxis = 'y1';
	  visible = 'true';
	} else if (key in config['y2']) {
	  yaxis = 'y2';
	  visible = 'true';
	}
        all[key] = {
	  x: [],
	  y: [],
	  mode: 'line',
	  name: key,
	  visible: visible,
	  yaxis: yaxis,
	  // Tell plot.ly to show the full series string.
	  hoverlabel: {namelength: -1},
	}
      }
      // Add this new timestamp+value to the series.
      all[key].x.push(time);
      all[key].y.push(row[key]);
    }
  }

  var layout = {
    title: title,
    xaxis: {title: 'Time (seconds since robot boot)'},
    yaxis: {title: 'Values'},
    hovermode: 'closest',
    yaxis2: {
      title: 'Values',
      overlaying: 'y',
      side: 'right'
    },
    legend: {
      orientation: "v",
      x: 1.1,  // Push it out of the way of the right y-axis
      xanchor: 'left',
    },
  };

  var data = [];
  for (var key in all) {
    data.push(all[key]);
  }
  Plotly.newPlot('chart1', data, layout,
		 {showLink: false});

  // Update the address bar when the selection changes.
  var div = document.getElementById("chart1")
  div.on('plotly_legendclick', function(e){
    console.log(e);
    var y1Series = [];  // Left y-axis series for updating the addressbar.
    var y2Series = [];  // Right y-axis series for updating the addressbar.
    var changedIndex = e.curveNumber;
    var updateLegend = true;
    for (var i = 0; i < e.data.length; i++) {
      var entry = e.data[i];
      //console.log(entry.name + " = " + entry.visible);
      if (i == changedIndex) {
	// This one changed and the entry is the old value.
	if (entry.visible == "legendonly") {
	  // Make it visible now. This is the inverse of below.
	  y1Series.push(entry.name);
	  // Tell plotly to update this series to put it on the left y-axis.
          Plotly.restyle('chart1', { yaxis: "y1" }, [changedIndex]);
	} else {
	  // It was already visible, maybe swap y-axis if needed?
	  if (data[i].yaxis != "y2") {
	    // Was on the left hand y axis, swap to the right hand y-axis.
	    // Don't update the legend, because it should still show this time.
	    updateLegend = false;
	    y2Series.push(entry.name);
	    Plotly.restyle('chart1', { yaxis: "y2" }, [changedIndex]);
	  } else {
	    // It was already on the y2 axis, allow it to become invisible.
	  }
	}
	continue;
      }
      // Not the selected entry, don't include it in the list of active series.
      if (entry.visible == "legendonly") continue;
      if (entry.yaxis == "y2") {
	y2Series.push(entry.name);
      } else {
	y1Series.push(entry.name);
      }
    }
    // Update the address.
    location.hash = "#y1=" + y1Series.join("+") + ",y2=" + y2Series.join("+")
    console.log(location.hash);
    return updateLegend;  // Should this click update the legend?
  });
}

// From the anchor in the address bar, load in the graph config.
  // Format: #y1=series1+series3,y2=series4,series5
  // where
  //   series1 and series3 would show on the left y-axis
  //   series2 would only show in the legend
  //   series4 and series5 would show against the right y-axis
function getConfig(chartNum) {
  var config = {  // Default, empty config.
    "y1": {},
    "y2": {}
  };
  console.log("anchor="+location.hash);
  var anchor = location.hash;
  if (!anchor.startsWith("#")) return config;  // No anchor.
  // Trim off leading #
  anchor = anchor.substr(1);
  // Split by "," to find the different parameters
  var entries = anchor.split(",");
  for (var e = 0; e < entries.length; e++) {
    if (entries.length <= 0) continue;
    // Split by "="
    var entry = entries[e].split("=");
    if (entry.length < 2) continue;
    var param = entry[0];
    var value = entry[1];
    if (param == "y1" || param == "y2") {
      // Split by "+"
      var visible = {};  // Empty by default
      var series = value.split("+");
      for (var i = 0; i < series.length; i++) {
        visible[series[i]] = true;
      }
      config[param] = visible;
    }
  }
  return config;
}
