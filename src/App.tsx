import { useState, useEffect } from 'react';
import { Header } from './components/Header';
import { Hero } from './components/Hero';
import { ToolCard } from './components/ToolCard';
import { ToolModal } from './components/ToolModal';
import { ThemeModal } from './components/ThemeModal';
import { LegalModal } from './components/LegalModal';
import { Footer } from './components/Footer';
import { THEME_PRESETS, DEFAULT_THEME } from './utils/themePresets';
import { TOOLS_CONFIG } from './utils/translations';
import type { ToolMeta } from './utils/translations';
import type { Language, ThemePreset } from './types';

export function App() {
  // 1. Language State
  const [language, setLanguage] = useState<Language>(() => {
    return (localStorage.getItem('bodlai_lang') as Language) || 'en';
  });

  // 2. Theme State
  const [currentTheme, setCurrentTheme] = useState<ThemePreset>(() => {
    const savedId = localStorage.getItem('bodlai_theme_id');
    const found = THEME_PRESETS.find((t) => t.id === savedId);
    return found || DEFAULT_THEME;
  });

  // 3. Modals State
  const [selectedTool, setSelectedTool] = useState<ToolMeta | null>(null);
  const [isThemeModalOpen, setIsThemeModalOpen] = useState(false);
  const [isLegalModalOpen, setIsLegalModalOpen] = useState(false);
  const [legalTab, setLegalTab] = useState<'privacy' | 'terms' | 'about'>('privacy');

  // Sync Language to LocalStorage
  const handleLanguageChange = (lang: Language) => {
    setLanguage(lang);
    localStorage.setItem('bodlai_lang', lang);
  };

  // Sync Theme to LocalStorage & Apply CSS variables
  const handleThemeChange = (theme: ThemePreset) => {
    setCurrentTheme(theme);
    localStorage.setItem('bodlai_theme_id', theme.id);
  };

  useEffect(() => {
    const root = document.documentElement;
    root.style.setProperty('--bg-top', currentTheme.bgTop);
    root.style.setProperty('--bg-mid', currentTheme.bgMid);
    root.style.setProperty('--bg-bottom', currentTheme.bgBottom);
    root.style.setProperty('--primary-accent', currentTheme.primaryAccent);
    root.style.setProperty('--warm-accent', currentTheme.warmAccent);
    root.style.setProperty('--surface-card', currentTheme.surfaceCard);
    root.style.setProperty('--surface-border', currentTheme.surfaceBorder);
    root.style.setProperty('--surface-subtle', currentTheme.surfaceSubtle);
    root.style.setProperty('--text-primary', currentTheme.textPrimary);
    root.style.setProperty('--text-secondary', currentTheme.textSecondary);
    root.style.setProperty('--card-glow', currentTheme.cardGlow);
  }, [currentTheme]);

  // Desktop PC Keyboard Shortcuts & Drag-Drop listeners
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // Ctrl+T / Cmd+T: Theme Modal
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 't') {
        e.preventDefault();
        setIsThemeModalOpen((prev) => !prev);
      }
      // Ctrl+L / Cmd+L: Language Switcher
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'l') {
        e.preventDefault();
        setLanguage((prev) => {
          const next = prev === 'en' ? 'bn' : 'en';
          localStorage.setItem('bodlai_lang', next);
          return next;
        });
      }
      // Esc: Close any active modal
      if (e.key === 'Escape') {
        setSelectedTool(null);
        setIsThemeModalOpen(false);
        setIsLegalModalOpen(false);
      }
    };

    // Global dragover prevent default to allow drop anywhere
    const handleGlobalDragOver = (e: DragEvent) => {
      e.preventDefault();
    };

    // Global drop handler to auto-select tool
    const handleGlobalDrop = (e: DragEvent) => {
      e.preventDefault();
      if (selectedTool) return; // If a tool modal is already open, let the modal handle it
      if (e.dataTransfer && e.dataTransfer.files.length > 0) {
        const file = e.dataTransfer.files[0];
        const name = file.name.toLowerCase();
        if (name.endsWith('.pdf')) {
          const tool = TOOLS_CONFIG.find((t) => t.id === 'pdf-to-pptx') || TOOLS_CONFIG[0];
          setSelectedTool(tool);
        } else if (name.endsWith('.png') || name.endsWith('.jpg') || name.endsWith('.jpeg') || name.endsWith('.webp')) {
          const tool = TOOLS_CONFIG.find((t) => t.id === 'image-to-pdf') || TOOLS_CONFIG[2];
          setSelectedTool(tool);
        } else if (name.endsWith('.txt')) {
          const tool = TOOLS_CONFIG.find((t) => t.id === 'text-to-pdf') || TOOLS_CONFIG[5];
          setSelectedTool(tool);
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    window.addEventListener('dragover', handleGlobalDragOver);
    window.addEventListener('drop', handleGlobalDrop);

    return () => {
      window.removeEventListener('keydown', handleKeyDown);
      window.removeEventListener('dragover', handleGlobalDragOver);
      window.removeEventListener('drop', handleGlobalDrop);
    };
  }, [selectedTool]);

  const openLegalModalWithTab = (tab: 'privacy' | 'terms' | 'about' = 'privacy') => {
    setLegalTab(tab);
    setIsLegalModalOpen(true);
  };

  return (
    <div
      className="min-h-screen flex flex-col relative transition-colors duration-500 overflow-x-hidden"
      style={{
        background: `linear-gradient(180deg, ${currentTheme.bgTop} 0%, ${currentTheme.bgMid} 45%, ${currentTheme.bgBottom} 100%)`,
        color: currentTheme.textPrimary,
      }}
    >
      {/* Dynamic Ambient Background Highlights */}
      <div
        className="fixed top-0 left-1/4 w-[500px] h-[500px] rounded-full blur-[160px] opacity-20 pointer-events-none transition-all duration-700"
        style={{ backgroundColor: currentTheme.primaryAccent }}
      />
      <div
        className="fixed bottom-0 right-1/4 w-[500px] h-[500px] rounded-full blur-[160px] opacity-15 pointer-events-none transition-all duration-700"
        style={{ backgroundColor: currentTheme.warmAccent }}
      />

      {/* Main App Header */}
      <Header
        language={language}
        onLanguageChange={handleLanguageChange}
        currentTheme={currentTheme}
        onOpenThemeModal={() => setIsThemeModalOpen(true)}
        onOpenLegalModal={openLegalModalWithTab}
      />

      {/* Hero Section */}
      <main className="flex-1">
        <Hero
          language={language}
          currentTheme={currentTheme}
          onOpenLegalModal={() => openLegalModalWithTab('privacy')}
        />

        {/* 6 Converters Grid Section */}
        <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pb-16">
          <div className="flex items-center justify-between mb-8 px-1">
            <div>
              <h2 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
                {language === 'bn' ? 'কনভার্টার টুলস (PC WebApp)' : 'Conversion Tools (PC WebApp)'}
              </h2>
              <p className="text-xs sm:text-sm text-slate-400 mt-1">
                {language === 'bn'
                  ? 'সব টুলস ১০০% ব্রাউজার মেমরিতে চলে — কোনো আপলোড লিমিট নেই'
                  : 'All 6 tools run locally inside your browser — zero upload wait'}
              </p>
            </div>
            <div className="hidden sm:flex items-center space-x-2">
              <span className="px-3 py-1 rounded-full text-xs font-semibold bg-white/5 border border-white/10 text-slate-300">
                ⌨️ Ctrl+T Themes • Ctrl+L Language
              </span>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {TOOLS_CONFIG.map((tool) => (
              <ToolCard
                key={tool.id}
                tool={tool}
                language={language}
                currentTheme={currentTheme}
                onSelect={(selected) => setSelectedTool(selected)}
              />
            ))}
          </div>
        </section>
      </main>

      {/* Modals */}
      <ToolModal
        tool={selectedTool}
        language={language}
        currentTheme={currentTheme}
        onClose={() => setSelectedTool(null)}
      />

      <ThemeModal
        isOpen={isThemeModalOpen}
        onClose={() => setIsThemeModalOpen(false)}
        currentTheme={currentTheme}
        onSelectTheme={handleThemeChange}
        language={language}
      />

      <LegalModal
        isOpen={isLegalModalOpen}
        initialTab={legalTab}
        onClose={() => setIsLegalModalOpen(false)}
        language={language}
        currentTheme={currentTheme}
      />

      {/* Footer */}
      <Footer
        language={language}
        currentTheme={currentTheme}
        onOpenLegalModal={openLegalModalWithTab}
      />
    </div>
  );
}

export default App;
