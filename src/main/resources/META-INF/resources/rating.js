document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.rating i').forEach(star => {
    star.addEventListener('click', async (e) => {
      e.stopPropagation();
      const container = star.parentElement;
      const id = container.dataset.id;
      const currentRate = parseInt(container.dataset.rate || '0');
      const newValue = parseInt(star.dataset.value);
      
      let rateToSend = newValue;
      if (currentRate === newValue) {
        rateToSend = 0;
      }

      try {
        const response = await fetch(`/fit/activities/id/${id}/rate`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(rateToSend)
        });

        if (response.ok) {
          const updated = await response.json();
          container.dataset.rate = updated.rate;
          container.querySelectorAll('i').forEach(s => {
            const val = parseInt(s.dataset.value);
            if (val <= updated.rate && updated.rate > 0) {
              s.classList.add('active');
            } else {
              s.classList.remove('active');
            }
          });
        } else {
          alert("Fehler beim Speichern der Bewertung");
        }
      } catch (err) {
        console.error(err);
        alert("Fehler beim Speichern der Bewertung");
      }
    });
  });
});
