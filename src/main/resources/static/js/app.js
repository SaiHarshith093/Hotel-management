(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', init);

    function init() {
        initSidebar();
        initDateTime();
        initNotifications();
        initUserAvatar();
        initActiveNav();
        initDeleteConfirm();
        initSuccessMessages();
        initTableEnhancements();
        initFormValidation();
    }

    /* Sidebar */
    function initSidebar() {
        var sidebar = document.getElementById('sidebar');
        var overlay = document.getElementById('sidebarOverlay');
        var toggle = document.getElementById('sidebarToggle');
        var close = document.getElementById('sidebarClose');

        if (!sidebar) return;

        function openSidebar() {
            sidebar.classList.add('open');
            if (overlay) overlay.classList.add('visible');
        }

        function closeSidebar() {
            sidebar.classList.remove('open');
            if (overlay) overlay.classList.remove('visible');
        }

        if (toggle) toggle.addEventListener('click', openSidebar);
        if (close) close.addEventListener('click', closeSidebar);
        if (overlay) overlay.addEventListener('click', closeSidebar);

        sidebar.querySelectorAll('.sidebar-link[data-nav]').forEach(function (link) {
            link.addEventListener('click', function () {
                if (window.innerWidth <= 768) closeSidebar();
            });
        });
    }

    /* Active navigation */
    function initActiveNav() {
        var path = window.location.pathname;
        document.querySelectorAll('.sidebar-link[data-nav]').forEach(function (link) {
            var nav = link.getAttribute('data-nav');
            if (nav && (path === nav || path.startsWith(nav + '/'))) {
                link.classList.add('active');
            }
        });
    }

    /* Date / time */
    function initDateTime() {
        var el = document.getElementById('topbarDatetime');
        if (!el) return;

        function update() {
            var now = new Date();
            var options = {
                weekday: 'short',
                year: 'numeric',
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit'
            };
            el.textContent = now.toLocaleString(undefined, options);
        }

        update();
        setInterval(update, 1000);
    }

    /* Notifications dropdown */
    function initNotifications() {
        var btn = document.getElementById('notificationsBtn');
        var dropdown = document.getElementById('notificationsDropdown');
        if (!btn || !dropdown) return;

        btn.addEventListener('click', function (e) {
            e.stopPropagation();
            var isOpen = !dropdown.hidden;
            dropdown.hidden = isOpen;
            btn.setAttribute('aria-expanded', String(!isOpen));
        });

        document.addEventListener('click', function () {
            dropdown.hidden = true;
            btn.setAttribute('aria-expanded', 'false');
        });

        dropdown.addEventListener('click', function (e) {
            e.stopPropagation();
        });
    }

    /* User avatar initials */
    function initUserAvatar() {
        var avatar = document.getElementById('userAvatar');
        var nameEl = document.querySelector('.topbar-user-name');
        if (!avatar || !nameEl) return;

        var name = nameEl.textContent.trim();
        if (name) {
            avatar.textContent = name.charAt(0).toUpperCase();
        }
    }

    /* Delete confirmation modal */
    function initDeleteConfirm() {
        var modal = document.getElementById('confirmModal');
        var messageEl = document.getElementById('confirmModalMessage');
        var actionBtn = document.getElementById('confirmModalAction');
        var pendingForm = null;

        if (!modal) return;

        document.querySelectorAll('form[data-confirm-delete]').forEach(function (form) {
            form.addEventListener('submit', function (e) {
                e.preventDefault();
                pendingForm = form;
                var msg = form.getAttribute('data-confirm-message') || 'Are you sure you want to delete this item?';
                if (messageEl) messageEl.textContent = msg;
                openModal(modal);
            });
        });

        if (actionBtn) {
            actionBtn.addEventListener('click', function () {
                if (pendingForm) {
                    pendingForm.removeAttribute('data-confirm-delete');
                    pendingForm.submit();
                }
                closeModal(modal);
            });
        }

        bindModalClose(modal, function () {
            pendingForm = null;
        });
    }

    /* Success messages */
    function initSuccessMessages() {
        document.querySelectorAll('[data-success-message]').forEach(function (el) {
            var msg = el.textContent.trim();
            if (msg) {
                showToast(msg, 'success');
                el.hidden = true;
            }
        });

        document.querySelectorAll('[data-error-message]').forEach(function (el) {
            var msg = el.textContent.trim();
            if (msg) {
                showToast(msg, 'error');
                el.hidden = true;
            }
        });
    }

    function showToast(message, type) {
        var container = document.getElementById('toastContainer');
        if (!container) {
            var modal = document.getElementById('successModal');
            var msgEl = document.getElementById('successModalMessage');
            if (modal && msgEl) {
                msgEl.textContent = message;
                openModal(modal);
            }
            return;
        }

        var toast = document.createElement('div');
        toast.className = 'toast' + (type === 'error' ? ' toast-error' : '');
        toast.textContent = message;
        container.appendChild(toast);

        setTimeout(function () {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            toast.style.transition = 'opacity 0.3s, transform 0.3s';
            setTimeout(function () { toast.remove(); }, 300);
        }, 4000);
    }

    /* Modal helpers */
    function openModal(modal) {
        modal.hidden = false;
        document.body.style.overflow = 'hidden';
    }

    function closeModal(modal) {
        modal.hidden = true;
        document.body.style.overflow = '';
    }

    function bindModalClose(modal, onClose) {
        modal.querySelectorAll('[data-modal-close]').forEach(function (btn) {
            btn.addEventListener('click', function () {
                closeModal(modal);
                if (onClose) onClose();
            });
        });

        modal.addEventListener('click', function (e) {
            if (e.target === modal) {
                closeModal(modal);
                if (onClose) onClose();
            }
        });
    }

    document.querySelectorAll('.modal-overlay').forEach(function (modal) {
        if (modal.id !== 'confirmModal') {
            bindModalClose(modal);
        }
    });

    /* Table search and pagination */
    function initTableEnhancements() {
        document.querySelectorAll('.table-card').forEach(function (card) {
            var table = card.querySelector('.data-table');
            if (!table) return;

            var tbody = table.querySelector('tbody');
            if (!tbody) return;

            var rows = Array.from(tbody.querySelectorAll('tr')).filter(function (r) {
                return !r.querySelector('.empty-row');
            });

            if (rows.length === 0) return;

            var toolbar = document.createElement('div');
            toolbar.className = 'table-toolbar';

            var searchWrap = document.createElement('div');
            searchWrap.className = 'table-search';
            searchWrap.innerHTML = '<span class="table-search-icon" aria-hidden="true">&#128269;</span>' +
                '<input type="search" placeholder="Search table..." aria-label="Search table">';

            var info = document.createElement('span');
            info.className = 'table-info';

            toolbar.appendChild(searchWrap);
            toolbar.appendChild(info);

            var responsive = card.querySelector('.table-responsive');
            card.insertBefore(toolbar, responsive);

            var pagination = document.createElement('div');
            pagination.className = 'table-pagination';
            pagination.innerHTML =
                '<span class="pagination-info"></span>' +
                '<div class="pagination-controls">' +
                '<button type="button" class="pagination-btn" data-page="prev">Prev</button>' +
                '<div class="pagination-pages"></div>' +
                '<button type="button" class="pagination-btn" data-page="next">Next</button>' +
                '</div>';
            card.appendChild(pagination);

            var searchInput = searchWrap.querySelector('input');
            var pageSize = 10;
            var currentPage = 1;
            var filteredRows = rows.slice();

            function render() {
                var total = filteredRows.length;
                var totalPages = Math.max(1, Math.ceil(total / pageSize));
                if (currentPage > totalPages) currentPage = totalPages;

                rows.forEach(function (r) { r.classList.add('row-hidden'); });

                var start = (currentPage - 1) * pageSize;
                var end = start + pageSize;
                filteredRows.slice(start, end).forEach(function (r) {
                    r.classList.remove('row-hidden');
                });

                info.textContent = total + ' record' + (total !== 1 ? 's' : '');
                var infoEl = pagination.querySelector('.pagination-info');
                if (infoEl) {
                    infoEl.textContent = 'Showing ' + (total === 0 ? 0 : start + 1) + '–' +
                        Math.min(end, total) + ' of ' + total;
                }

                var pagesEl = pagination.querySelector('.pagination-pages');
                pagesEl.innerHTML = '';
                for (var i = 1; i <= totalPages; i++) {
                    if (totalPages > 7 && Math.abs(i - currentPage) > 2 && i !== 1 && i !== totalPages) {
                        if (i === 2 || i === totalPages - 1) {
                            var dots = document.createElement('span');
                            dots.textContent = '…';
                            dots.style.padding = '0 0.25rem';
                            pagesEl.appendChild(dots);
                        }
                        continue;
                    }
                    var btn = document.createElement('button');
                    btn.type = 'button';
                    btn.className = 'pagination-btn' + (i === currentPage ? ' active' : '');
                    btn.textContent = i;
                    btn.dataset.pageNum = i;
                    btn.addEventListener('click', function () {
                        currentPage = parseInt(this.dataset.pageNum, 10);
                        render();
                    });
                    pagesEl.appendChild(btn);
                }

                pagination.querySelector('[data-page="prev"]').disabled = currentPage <= 1;
                pagination.querySelector('[data-page="next"]').disabled = currentPage >= totalPages;
            }

            searchInput.addEventListener('input', function () {
                var q = this.value.toLowerCase().trim();
                filteredRows = rows.filter(function (row) {
                    return row.textContent.toLowerCase().includes(q);
                });
                currentPage = 1;
                render();
            });

            pagination.querySelector('[data-page="prev"]').addEventListener('click', function () {
                if (currentPage > 1) { currentPage--; render(); }
            });

            pagination.querySelector('[data-page="next"]').addEventListener('click', function () {
                var totalPages = Math.ceil(filteredRows.length / pageSize);
                if (currentPage < totalPages) { currentPage++; render(); }
            });

            render();
        });
    }

    /* Form validation styling */
    function initFormValidation() {
        document.querySelectorAll('.field-error').forEach(function (error) {
            var group = error.closest('.form-group');
            if (group) group.classList.add('has-error');
        });

        document.querySelectorAll('form').forEach(function (form) {
            form.querySelectorAll('input, select, textarea').forEach(function (field) {
                field.addEventListener('invalid', function () {
                    var group = field.closest('.form-group');
                    if (group) group.classList.add('has-error');
                });

                field.addEventListener('input', function () {
                    var group = field.closest('.form-group');
                    if (group && field.checkValidity()) {
                        group.classList.remove('has-error');
                    }
                });
            });
        });
    }
})();
