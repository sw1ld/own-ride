function showConfirm(title, message, onConfirm) {
  const modal = document.getElementById('confirmModal');
  const modalTitle = document.getElementById('confirmModalTitle');
  const modalMessage = document.getElementById('confirmModalMessage');
  const modalConfirmBtn = document.getElementById('confirmModalAction');
  const modalCancelBtn = document.getElementById('cancelModal');

  const closeModal = () => {
    modal.classList.remove('is-active');
  };

  modalTitle.textContent = title;
  modalMessage.textContent = message;
  modalConfirmBtn.textContent = 'Confirm';
  if (modalCancelBtn) modalCancelBtn.style.display = '';
  modal.classList.add('is-active');

  modalConfirmBtn.onclick = async () => {
    if (onConfirm) await onConfirm();
    closeModal();
  };

  document.getElementById('closeModal').onclick = closeModal;
  if (modalCancelBtn) modalCancelBtn.onclick = closeModal;
  document.querySelector('.modal-background').onclick = closeModal;
}

function showAlert(title, message) {
  const modal = document.getElementById('confirmModal');
  const modalTitle = document.getElementById('confirmModalTitle');
  const modalMessage = document.getElementById('confirmModalMessage');
  const modalConfirmBtn = document.getElementById('confirmModalAction');
  const modalCancelBtn = document.getElementById('cancelModal');

  const closeModal = () => {
    modal.classList.remove('is-active');
    if (modalCancelBtn) modalCancelBtn.style.display = '';
    modalConfirmBtn.textContent = 'Confirm';
  };

  modalTitle.textContent = title;
  modalMessage.textContent = message;
  modalConfirmBtn.textContent = 'OK';
  if (modalCancelBtn) modalCancelBtn.style.display = 'none';
  modal.classList.add('is-active');

  modalConfirmBtn.onclick = () => {
    closeModal();
  };

  document.getElementById('closeModal').onclick = closeModal;
  document.querySelector('.modal-background').onclick = closeModal;
}

document.addEventListener('DOMContentLoaded', () => {
  const saveBike = async (row) => {
    if (!row) return;
    const id = row.dataset.bikeId;
    const producer = row.querySelector('.bike-producer')?.value.trim() ?? '';
    const name = row.querySelector('.bike-name')?.value.trim() ?? '';

    if (!producer || !name) {
      showAlert('Validation Error', 'Producer and bike name must not be empty.');
      return;
    }

    try {
      const params = new URLSearchParams({ producer, name });

      const response = await fetch('/own/bikes/id/' + id, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: params.toString()
      });

      if (response.ok) {
        window.location.reload();
      } else {
        showAlert('Error', 'Failed to save bike.');
      }
    } catch (err) {
      console.error(err);
      showAlert('Error', 'Failed to save bike.');
    }
  };

  document.querySelectorAll('.save-bike').forEach(btn => {
    btn.addEventListener('click', () => saveBike(btn.closest('tr')));
  });

  document.querySelectorAll('.bike-producer, .bike-name').forEach(input => {
    input.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') {
        e.preventDefault();
        saveBike(input.closest('tr'));
      }
    });
  });

  document.querySelectorAll('.delete-bike').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const id = btn.dataset.id;
      showConfirm("Confirm Deletion", "Do you really want to delete this bike and all its activity assignments?", async () => {
        try {
          const response = await fetch(`/own/bikes/id/${id}`, { method: 'DELETE' });
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

  const addForm = document.querySelector('form[action="/own/bikes"]');
  if (addForm) {
    addForm.addEventListener('submit', (e) => {
      const producer = addForm.querySelector('input[name="producer"]')?.value.trim() ?? '';
      const name = addForm.querySelector('input[name="name"]')?.value.trim() ?? '';
      if (!producer || !name) {
        e.preventDefault();
        showAlert('Validation Error', 'Producer and bike name must not be empty.');
      }
    });
  }

  document.querySelectorAll('.delete-route').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const id = btn.dataset.id;
      showConfirm("Confirm Deletion", "Do you really want to delete this route?", async () => {
        try {
          const response = await fetch(`/own/activities/id/${id}`, { method: 'DELETE' });
          if (response.ok) {
            sessionStorage.setItem('feed_needs_reload', 'true');
            sessionStorage.removeItem('feed_restore_html');
            sessionStorage.removeItem('feed_restore_scroll');
            sessionStorage.removeItem('pending_rating_updates');
            sessionStorage.removeItem('pending_name_updates');
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

  document.querySelectorAll('.remove-from-group').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const activityId = btn.dataset.activityId;
      const groupElement = document.querySelector('.editable-group-name');
      const groupId = groupElement ? groupElement.dataset.id : null;

      if (!groupId) {
        console.error("Group ID not found");
        return;
      }

      showConfirm('Remove from Group', 'Do you really want to remove this activity from the group?', async () => {
        try {
          const response = await fetch(`/own/groups/id/${groupId}`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(activityId)
          });
          if (response.ok) {
            sessionStorage.setItem('feed_needs_reload', 'true');
            sessionStorage.removeItem('feed_restore_html');
            sessionStorage.removeItem('feed_restore_scroll');
            sessionStorage.removeItem('pending_rating_updates');
            sessionStorage.removeItem('pending_name_updates');
            if (response.headers.get('X-Group-Dissolved') === 'true') {
              window.location.href = '/own/activities';
            } else {
              window.location.reload();
            }
          } else {
            alert('Failed to remove activity from group');
          }
        } catch (err) {
          console.error(err);
          alert('Failed to remove activity from group');
        }
      });
    });
  });
});
