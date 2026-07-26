function getChartTheme() {
    const style = getComputedStyle(document.documentElement);
    const primaryColor = style.getPropertyValue('--primary-color').trim() || '#0B4587';
    const subtitleColor = style.getPropertyValue('--subtitle-color').trim() || primaryColor;
    
    return {
        primaryColor,
        subtitleColor,
        barColor: primaryColor + '99', // ~60% opacity
        lineBgColor: primaryColor + '33', // ~20% opacity
        slopeVeryEasy: style.getPropertyValue('--slope-easy').trim() || '#add8e6',
        slopeEasy: style.getPropertyValue('--slope-easy').trim() || '#87ceeb',
        slopeMedium: style.getPropertyValue('--slope-medium').trim() || '#4682b4',
        slopeHard: style.getPropertyValue('--slope-hard').trim() || '#1e0bd0',
        slopeVeryHard: style.getPropertyValue('--slope-very-hard').trim() || '#1f104f',
        fontFamily: "'Inter', sans-serif"
    };
}

function getBaseOptions(xLabel, yLabel, options = {}) {
    const theme = getChartTheme();
    const beginAtZero = options.beginAtZero !== undefined ? options.beginAtZero : true;
    
    return {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                display: false
            }
        },
        scales: {
            y: {
                beginAtZero: beginAtZero,
                grid: {
                    display: false
                },
                ticks: {
                    color: theme.subtitleColor,
                    precision: 0
                },
                title: {
                    display: !!yLabel,
                    text: yLabel,
                    color: theme.subtitleColor,
                    font: {
                        family: theme.fontFamily
                    }
                }
            },
            x: {
                grid: {
                    display: false
                },
                ticks: {
                    color: theme.subtitleColor
                },
                title: {
                    display: !!xLabel,
                    text: xLabel,
                    color: theme.subtitleColor,
                    font: {
                        family: theme.fontFamily
                    }
                }
            }
        }
    };
}

function createChart(canvasId, config) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return null;
    return new Chart(ctx, config);
}
