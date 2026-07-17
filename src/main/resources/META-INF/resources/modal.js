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
  document.querySelectorAll('.delete-bike').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const id = btn.dataset.id;
      showConfirm("Confirm Deletion", "Do you really want to delete this bike and all its activity assignments?", async () => {
        try {
          const response = await fetch(`/own/bikes/${id}`, { method: 'DELETE' });
          if (response.ok) {
            window.location.reload();
          } else {
            alert("Error during deletion");
          }
        } catch (err) {
          console.error(err);
          alert("Error during deletion");
        }
      });
    });
  });

  document.querySelectorAll('.delete-route').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const id = btn.dataset.id;
      showConfirm("Confirm Deletion", "Do you really want to delete this route?", async () => {
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
            alert("Error during deletion");
          }
        } catch (err) {
          console.error(err);
          alert("Error during deletion");
        }
      });
    });
  });

  document.querySelectorAll('.recalculate-route').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const id = btn.dataset.id;
      showConfirm("Confirm Recalculation", "Do you really want to recalculate this route?", async () => {
        try {
          const response = await fetch(`/own/activities/id/${id}`, { method: 'PUT' });
          if (response.ok) {
            window.location.reload();
          } else {
            alert("Error during recalculation");
          }
        } catch (err) {
          console.error(err);
          alert("Error during recalculation");
        }
      });
    });
  });
});
