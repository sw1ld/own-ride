document.addEventListener('DOMContentLoaded', () => {
    document.addEventListener('click', async (e) => {
        const star = e.target.closest('.rating i');
        if (!star) return;

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
            const url = container.dataset.type === 'GROUP'
                ? `/own/groups/id/${id}/rate`
                : `/own/activities/id/${id}/rate`;

            const response = await fetch(url, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(rateToSend)
            });

            if (response.ok) {
                const updated = await response.json();
                const newRate = updated.rate;

                document.querySelectorAll(`.rating[data-id="${id}"][data-type="${container.dataset.type}"]`).forEach(el => {
                    el.dataset.rate = newRate;
                    el.querySelectorAll('i').forEach(s => {
                        const val = parseInt(s.dataset.value);
                        s.classList.toggle('active', val <= newRate && newRate > 0);
                    });
                });
            } else {
                alert("Error saving rating");
            }
        } catch (err) {
            console.error(err);
            alert("Error saving rating");
        }
    });
});
