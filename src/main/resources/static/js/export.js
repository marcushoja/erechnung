(() => {
    const exportForm = document.getElementById("export-form");
    const validationErrors = document.getElementById("validation-errors");
    const errorAlertSection = document.getElementById("error-alert-section");

    if (!exportForm) {
        return;
    }

    exportForm.addEventListener("submit", () => {
        if (validationErrors) {
            validationErrors.remove();
        }
        if (errorAlertSection) {
            errorAlertSection.remove();
        }
    });
})();
