document.addEventListener('DOMContentLoaded', function () {
    const foodItemSelect = document.getElementById('foodItemId');
    const quantityInput = document.getElementById('quantity');
    const amountDisplay = document.getElementById('orderAmount');

    if (!foodItemSelect || !quantityInput || !amountDisplay) {
        return;
    }

    function updateAmount() {
        const selectedOption = foodItemSelect.options[foodItemSelect.selectedIndex];
        const price = parseFloat(selectedOption.getAttribute('data-price')) || 0;
        const quantity = parseInt(quantityInput.value, 10) || 0;
        const total = price * quantity;
        amountDisplay.textContent = '₹' + total.toFixed(2);
    }

    foodItemSelect.addEventListener('change', updateAmount);
    quantityInput.addEventListener('input', updateAmount);
    updateAmount();
});
