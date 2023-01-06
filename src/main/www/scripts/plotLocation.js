function makeplot(filename) {
  console.log("Loading File: " + filename)
  Plotly.d3.csv(filename, function(data){ processData(data, filename) } );
};

function processData(allRows, filename) {
  var x = [], y = [];

  if (allRows == null) {
    alert("Unable to open CSV file: " + filename);
    return;
  }
  var actual = {
	name: "actual",
	x: [],
	y: [],
	mode: 'markers'
  }
  var desired = {
	name: "desired",
	x: [],
	y: [],
	mode: 'markers'
  }
  var traces = [
	  actual,
	  desired
  ];
  for (var i=0; i<allRows.length; i++) {
    row = allRows[i];
    actual.x.push( row['TrajectoryDrive/actual/x'] );
    actual.y.push( row['TrajectoryDrive/actual/y'] );
    desired.x.push( row['TrajectoryDrive/desired/x'] );
    desired.y.push( row['TrajectoryDrive/desired/y'] );
  }

  var layout = {
    title:'Location Graph',
    height: 720,
    width: 1280
  };
  Plotly.newPlot('myDiv', traces, layout);
};
