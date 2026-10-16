document.addEventListener('DOMContentLoaded', () => {
  // Theme Toggle
  const themeToggle = document.getElementById('theme-toggle');
  const currentTheme = localStorage.getItem('theme') || 'dark';
  document.documentElement.setAttribute('data-theme', currentTheme);
  updateThemeIcon(currentTheme);

  themeToggle.addEventListener('click', () => {
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    const newTheme = isDark ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    updateThemeIcon(newTheme);
  });

  function updateThemeIcon(theme) {
    themeToggle.innerHTML = theme === 'dark' 
      ? '☀️' 
      : '🌙';
  }

  // Tools Category Filter
  const filterTabs = document.querySelectorAll('.tab-btn');
  const toolCards = document.querySelectorAll('.tool-item-card');

  filterTabs.forEach(tab => {
    tab.addEventListener('click', () => {
      filterTabs.forEach(t => t.classList.remove('active'));
      tab.classList.add('active');

      const category = tab.getAttribute('data-category');

      toolCards.forEach(card => {
        if (category === 'all' || card.getAttribute('data-category') === category) {
          card.style.display = 'flex';
        } else {
          card.style.display = 'none';
        }
      });
    });
  });

  // Phone Mockup interactive chips
  const phoneChips = document.querySelectorAll('.phone-chip');
  const phoneCards = document.querySelectorAll('.phone-card');

  phoneChips.forEach(chip => {
    chip.addEventListener('click', () => {
      phoneChips.forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      const cat = chip.getAttribute('data-cat');
      phoneCards.forEach(card => {
        if (cat === 'all' || card.getAttribute('data-cat') === cat) {
          card.style.display = 'flex';
        } else {
          card.style.display = 'none';
        }
      });
    });
  });

  // Copy Code Snippet
  window.copySnippet = function(btn, text) {
    navigator.clipboard.writeText(text).then(() => {
      const original = btn.innerText;
      btn.innerText = 'Copied!';
      btn.style.background = '#43A047';
      setTimeout(() => {
        btn.innerText = original;
        btn.style.background = '';
      }, 2000);
    });
  };

  // FAQ Accordion
  const faqItems = document.querySelectorAll('.faq-item');
  faqItems.forEach(item => {
    item.addEventListener('click', () => {
      const isActive = item.classList.contains('active');
      faqItems.forEach(i => i.classList.remove('active'));
      if (!isActive) {
        item.classList.add('active');
      }
    });
  });
});
