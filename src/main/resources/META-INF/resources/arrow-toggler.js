document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('th.sortable').forEach((header, colIndex) => {
    header.addEventListener('click', () => {
      const table = header.closest('table');
      const tbody = table.querySelector('tbody');
      const rows = Array.from(tbody.querySelectorAll('tr'));

      let ascending;
      if (!header.sortState || header.sortState === 'none') {
        ascending = true;
        header.sortState = 'asc';
      } else if (header.sortState === 'asc') {
        ascending = false;
        header.sortState = 'desc';
      } else {
        ascending = null; // neutral
        header.sortState = 'none';
      }

      table.querySelectorAll('th.sortable').forEach(h => {
        h.sortState = h === header ? header.sortState : 'none';
        const arrowSpan = h.querySelector('.arrow');
        if (arrowSpan) {
            arrowSpan.textContent = '↑↓'; // default neutral symbol
        }
      });

      const arrow = header.querySelector('.arrow');
      if (arrow) {
          if (ascending === true) arrow.textContent = '↓';
          else if (ascending === false) arrow.textContent = '↑';
      }

      // If neutral, optionally skip sorting or restore original order (not implemented)
      if (ascending === null) return;

      rows.sort((a, b) => {
        const cellA = a.children[colIndex].textContent.trim();
        const cellB = b.children[colIndex].textContent.trim();

        const numA = parseFloat(cellA.replace(/[^0-9.-]/g, ''));
        const numB = parseFloat(cellB.replace(/[^0-9.-]/g, ''));

        if (!isNaN(numA) && !isNaN(numB)) {
          return ascending ? numA - numB : numB - numA;
        }
        return ascending ? cellA.localeCompare(cellB) : cellB.localeCompare(cellA);
      });

      rows.forEach(row => tbody.appendChild(row));
    });
  });
});
