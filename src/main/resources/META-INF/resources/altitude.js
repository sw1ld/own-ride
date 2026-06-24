let altitudeChart = null;
let currentFilteredRoute = [];
let routeOffset = 0;

function getDistance(p1, p2) {
  if (!p1 || !p2) return 0;
  const R = 6371; // Radius of the earth in km
  const dLat = (p2.lat - p1.lat) * Math.PI / 180;
  const dLon = (p2.lon - p1.lon) * Math.PI / 180;
  const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(p1.lat * Math.PI / 180) * Math.cos(p2.lat * Math.PI / 180) *
      Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c; // Distance in km
}

function updateAltitudeChart(route, distance) {
  const theme = getChartTheme();
  
  // Filter leading zero altitudes
  let firstNonZeroIndex = route.findIndex(p => p.altitude && p.altitude > 0);
  if (firstNonZeroIndex === -1) firstNonZeroIndex = 0;
  
  routeOffset = firstNonZeroIndex;
  const filteredRoute = route.slice(firstNonZeroIndex);
  const totalDistance = parseFloat(distance) || 0;

  // Subsampling to improve performance
  const maxPoints = 2000;
  const step = Math.max(1, Math.ceil(filteredRoute.length / maxPoints));
  
  currentFilteredRoute = [];
  for (let i = 0; i < filteredRoute.length; i += step) {
    const point = filteredRoute[i];
    point.originalIndex = i + firstNonZeroIndex;
    currentFilteredRoute.push(point);
  }
  // Ensure last point is included
  if (filteredRoute.length > 0 && (filteredRoute.length - 1) % step !== 0) {
      const lastPoint = filteredRoute[filteredRoute.length - 1];
      lastPoint.originalIndex = filteredRoute.length - 1 + firstNonZeroIndex;
      currentFilteredRoute.push(lastPoint);
  }

  // Smoothing Altitudes using moving average (window of 11)
  const rawAltitudes = currentFilteredRoute.map(p => {
    return (p.altitude !== undefined && p.altitude !== null) ? p.altitude : 0;
  });

  const altitudes = rawAltitudes.map((val, i, arr) => {
      const window = 5; // Erhöht auf 5 für glattere Höhen (11 Punkte)
      let values = [];
      for (let j = i - window; j <= i + window; j++) {
          if (j >= 0 && j < arr.length && arr[j] !== 0) {
              values.push(arr[j]);
          }
      }
      if (values.length === 0) return val;
      // Median filter for robustness
      values.sort((a, b) => a - b);
      const median = values[Math.floor(values.length / 2)];
      
      // Moving average of the window (excluding outliers could be better, but let's start with average of median-filtered window)
      const sum = values.reduce((a, b) => a + b, 0);
      return sum / values.length;
  });

  // Calculate distances for each point in currentFilteredRoute
  let cumulativeDistance = 0;
  const chartPoints = currentFilteredRoute.map((p, index) => {
    if (index > 0) {
      cumulativeDistance += getDistance(currentFilteredRoute[index - 1], p);
    }
    return { x: cumulativeDistance, y: altitudes[index] };
  });

  // Calculate slopes using a sliding window of approximately 40 meters
  const rawSlopes = [];
  const slopeWindowMeters = 40; 
  
  for (let i = 0; i < chartPoints.length; i++) {
    // Find a point roughly slopeWindowMeters behind
    let prevIdx = i - 1;
    while (prevIdx > 0 && (chartPoints[i].x - chartPoints[prevIdx].x) * 1000 < slopeWindowMeters) {
        prevIdx--;
    }
    
    if (prevIdx < 0) {
        rawSlopes.push(0);
        continue;
    }

    const dAlt = chartPoints[i].y - chartPoints[prevIdx].y;
    const dDist = (chartPoints[i].x - chartPoints[prevIdx].x) * 1000; // in meters
    
    let slope = 0;
    if (dDist > 5) { 
        slope = (dAlt / dDist) * 100;
    } else if (i > 0) {
        slope = rawSlopes[i-1];
    }
    rawSlopes.push(slope);
  }

  // Smooth slopes using moving average (window of 15)
  const slopes = rawSlopes.map((val, i, arr) => {
      const window = 7; // 15 Punkte gesamt
      let sum = 0;
      let count = 0;
      for (let j = i - window; j <= i + window; j++) {
          if (j >= 0 && j < arr.length) {
              sum += arr[j];
              count++;
          }
      }
      return sum / count;
  });

  const getSlopeColor = (slope) => {
    const absSlope = Math.abs(slope);
    if (absSlope <= 3) return theme.slopeEasy;
    if (absSlope <= 8) return theme.slopeMedium;
    if (absSlope <= 13) return theme.slopeHard;
    return theme.slopeVeryHard;
  };

  if (altitudeChart) {
    altitudeChart.destroy();
  }

  // Determine tick interval
  let interval = 1;
  if (totalDistance > 50) interval = 10;
  else if (totalDistance > 10) interval = 5;

  const options = getBaseOptions('distance', 'altitude', { beginAtZero: false });
  
  // Customizations for Altitude Profile
  options.plugins.tooltip = {
    enabled: true,
    intersect: false,
    mode: 'index',
    axis: 'x',
    callbacks: {
      title: function(context) {
        const dist = context[0].parsed.x;
        return `Distance: ${dist.toFixed(2)} km`;
      },
      label: function(context) {
        const index = context.dataIndex;
        const altitude = context.parsed.y;
        const slope = slopes[index];
        return [
            `Altitude: ${altitude.toFixed(1)} m`,
            `Slope: ${slope.toFixed(1)} %`
        ];
      }
    }
  };
  
  options.scales.x.type = 'linear';
  options.scales.x.min = 0;
  options.scales.x.max = totalDistance;
  options.scales.x.ticks = {
    color: theme.subtitleColor,
    callback: function(value) {
      return Math.round(value) + " km";
    },
    stepSize: interval,
    autoSkip: true,
    maxRotation: 0
  };
  
  options.hover = {
    mode: 'index',
    intersect: false,
    axis: 'x'
  };
  
  options.onHover = (event, chartElements) => {
    if (chartElements.length > 0) {
      const index = chartElements[0].index;
      const p = currentFilteredRoute[index];
      const originalIndex = p.originalIndex !== undefined ? p.originalIndex : index + routeOffset;
      
      if (window.showHoverPointOnMap) {
        window.showHoverPointOnMap(originalIndex);
      }
    } else {
      if (window.hideHoverPointOnMap) {
        window.hideHoverPointOnMap();
      }
    }
  };

  altitudeChart = createChart('altitudeChart', {
    type: 'line',
    data: {
      datasets: [{
        label: 'Altitude (m)',
        data: chartPoints,
        borderColor: theme.primaryColor,
        backgroundColor: theme.lineBgColor,
        segment: {
            borderColor: ctx => getSlopeColor(slopes[ctx.p1DataIndex]),
            backgroundColor: ctx => getSlopeColor(slopes[ctx.p1DataIndex]) + '33', // 20% opacity
        },
        fill: true,
        tension: 0.1,
        pointRadius: 0
      }]
    },
    options: options
  });
}

window.highlightAltitudePoint = function(originalIndex) {
    if (!altitudeChart) return;
    
    let chartIndex = -1;
    if (originalIndex !== -1) {
        // Find the closest point in currentFilteredRoute
        let minDiff = Infinity;
        for (let i = 0; i < currentFilteredRoute.length; i++) {
            const diff = Math.abs(currentFilteredRoute[i].originalIndex - originalIndex);
            if (diff < minDiff) {
                minDiff = diff;
                chartIndex = i;
            }
        }
        // Only highlight if it's reasonably close
        if (minDiff > 50) chartIndex = -1; 
    }

    if (chartIndex >= 0 && chartIndex < currentFilteredRoute.length) {
        altitudeChart.setActiveElements([{
            datasetIndex: 0,
            index: chartIndex
        }]);
        altitudeChart.tooltip.setActiveElements([{
            datasetIndex: 0,
            index: chartIndex
        }], {
            x: 0,
            y: 0
        });
        altitudeChart.update();
    } else {
        altitudeChart.setActiveElements([]);
        altitudeChart.tooltip.setActiveElements([], {x: 0, y: 0});
        altitudeChart.update();
    }
};
