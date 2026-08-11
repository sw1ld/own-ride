document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('[contenteditable="true"][data-id]').forEach(element => {
        element.addEventListener('blur', async () => {
            const id = element.dataset.id;
            const newName = element.innerText.trim();
            const type = element.classList.contains('editable-group-name') ? 'groups' : 'activities';
            
            try {
                const response = await fetch(`/own/${type}/id/${id}/name`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: newName
                });

                if (response.ok) {
                    const updated = await response.json();
                    const name = updated.name || updated.displayName;
                    element.innerText = name;
                    
                    // Update breadcrumb if present
                    const breadcrumbActive = document.querySelector('.breadcrumb li.is-active a');
                    if (breadcrumbActive) {
                        breadcrumbActive.innerText = name;
                    }
                } else {
                    alert("Error saving name");
                }
            } catch (err) {
                console.error(err);
                alert("Error saving name");
            }
        });

        element.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                element.blur();
            }
        });
    });
});
