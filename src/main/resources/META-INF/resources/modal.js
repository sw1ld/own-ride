function showConfirm(title, message, onConfirm) {
  const modal = document.getElementById('confirmModal');
  const modalTitle = document.getElementById('confirmModalTitle');
  const modalMessage = document.getElementById('confirmModalMessage');
  const modalConfirmBtn = document.getElementById('confirmModalAction');
  
  const closeModal = () => modal.classList.remove('is-active');

  modalTitle.textContent = title;
  modalMessage.textContent = message;
  modal.classList.add('is-active');

  modalConfirmBtn.onclick = async () => {
    await onConfirm();
    closeModal();
  };

  document.getElementById('closeModal').onclick = closeModal;
  document.getElementById('cancelModal').onclick = closeModal;
  document.querySelector('.modal-background').onclick = closeModal;
}

document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.delete-route').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const id = btn.dataset.id;
      showConfirm("Löschen bestätigen", "Möchtest du diese Route wirklich löschen?", async () => {
        try {
          const response = await fetch(`/own/activities/id/${id}`, { method: 'DELETE' });
          if (response.ok) {
            if (window.location.pathname.includes('/id/')) {
               // If we are on the detail page, go back to the list
               window.location.href = '/own/activities';
            } else {
               window.location.reload();
            }
          } else {
            alert("Fehler beim Löschen");
          }
        } catch (err) {
          console.error(err);
          alert("Fehler beim Löschen");
        }
      });
    });
  });

  document.querySelectorAll('.recalculate-route').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const id = btn.dataset.id;
      showConfirm("Neuberechnung bestätigen", "Möchtest du diese Route wirklich neu berechnen?", async () => {
        try {
          const response = await fetch(`/own/activities/id/${id}`, { method: 'PUT' });
          if (response.ok) {
            window.location.reload();
          } else {
            alert("Fehler bei der Neuberechnung");
          }
        } catch (err) {
          console.error(err);
          alert("Fehler bei der Neuberechnung");
        }
      });
    });
  });
});
