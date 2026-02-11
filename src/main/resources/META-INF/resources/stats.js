function createBarChart(canvasId, labels, data, label, title, color, barThickness) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return;
    
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels,
            datasets: [{
                label,
                data,
                backgroundColor: color,
                barThickness: barThickness
            }]
        },
        options: {
            responsive: true,
            plugins: {
                title: {
                    display: true,
                    text: title
                }
            }
        }
    });
}

async function loadStatistics() {
    const urlParams = new URLSearchParams(window.location.search);
    const year = urlParams.get('year') || new Date().getFullYear();

    try {
        const response = await fetch(`/fit/stats?year=${year}`, {
            headers: {
                'Accept': 'application/json'
            }
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const stats = await response.json();

        // stats.tourDates ist ein Objekt mit ISO-Datum als Key und Distanz als Value
        // Da wir eine TreeMap im Backend verwenden, kommen die Keys sortiert an.
        const dailyDates = Object.keys(stats.tourDates);
        const dailyDistance = Object.values(stats.tourDates);

        initStatistics(dailyDates, dailyDistance);

        // Update Summary if elements exist
        const ridesCount = document.getElementById('ridesCount');
        const totalDistance = document.getElementById('totalDistance');
        if (ridesCount) ridesCount.textContent = stats.rides;
        if (totalDistance) totalDistance.textContent = stats.distance;

    } catch (e) {
        console.error("Fehler beim Laden der Statistik-Daten", e);
    }
}

function initStatistics(dailyDates, dailyDistance) {
    createBarChart(
        "routesInYearChart",
        dailyDates,
        dailyDistance,
        'Kilometer',
        'Distribution over time',
        'rgba(54, 162, 235, 0.6)',
        5
    );

    const distances = dailyDistance.filter(v => v > 0);
    const binCount = 10;
    const min = 0;
    const max = 100;
    const binSize = (max - min) / binCount;

    const bins = new Array(binCount).fill(0).map((_, i) => ({
        start: min + i * binSize,
        end: min + (i + 1) * binSize,
        count: 0
    }));

    distances.forEach(value => {
        const index = Math.min(
            Math.floor((value - min) / binSize),
            binCount - 1
        );
        if (index >= 0) {
            bins[index].count++;
        }
    });

    const labelPerBin = bins.map(b => b.end.toFixed(1));
    const dataPerBin = bins.map(b => b.count);

    createBarChart(
        "routeLengthChart",
        labelPerBin,
        dataPerBin,
        '#routes',
        'Routes per kilometer',
        'rgba(75, 192, 192, 0.6)',
        50
    );
}
