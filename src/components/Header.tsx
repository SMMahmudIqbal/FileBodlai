import React, { useState, useEffect } from 'react';
import { Palette, ShieldCheck, Globe, Smartphone, MonitorDown, Laptop, Maximize, Minimize } from 'lucide-react';
import type { Language, ThemePreset } from '../types';
import { UI_STRINGS } from '../utils/translations';
import { GithubIcon } from './GithubIcon';

interface HeaderProps {
  language: Language;
  onLanguageChange: (lang: Language) => void;
  currentTheme: ThemePreset;
  onOpenThemeModal: () => void;
  onOpenLegalModal: (tab?: 'privacy' | 'terms' | 'about') => void;
}

export const Header: React.FC<HeaderProps> = ({
  language,
  onLanguageChange,
  currentTheme,
  onOpenThemeModal,
  onOpenLegalModal,
}) => {
  const t = UI_STRINGS[language];

  // PWA & Desktop PC States
  const [deferredPrompt, setDeferredPrompt] = useState<any>(null);
  const [isStandalone, setIsStandalone] = useState(false);
  const [isFullscreen, setIsFullscreen] = useState(false);

  useEffect(() => {
    // Check if running in standalone desktop app mode
    const isApp = window.matchMedia('(display-mode: standalone)').matches ||
                  (window.navigator as any).standalone === true;
    setIsStandalone(isApp);

    // Listen for PWA desktop install prompt
    const handleBeforeInstall = (e: Event) => {
      e.preventDefault();
      setDeferredPrompt(e);
    };

    window.addEventListener('beforeinstallprompt', handleBeforeInstall);

    // Listen for fullscreen change
    const handleFsChange = () => {
      setIsFullscreen(Boolean(document.fullscreenElement));
    };
    document.addEventListener('fullscreenchange', handleFsChange);

    return () => {
      window.removeEventListener('beforeinstallprompt', handleBeforeInstall);
      document.removeEventListener('fullscreenchange', handleFsChange);
    };
  }, []);

  const handleInstallClick = async () => {
    if (deferredPrompt) {
      deferredPrompt.prompt();
      const choiceResult = await deferredPrompt.userChoice;
      if (choiceResult.outcome === 'accepted') {
        setIsStandalone(true);
      }
      setDeferredPrompt(null);
    } else {
      alert(
        language === 'bn'
          ? 'এই অ্যাপটি আপনার পিসিতে ইনস্টল করতে ক্রোম বা এজ ব্রাউজারের অ্যাড্রেস বারের ডানপাশের "Install" বা (+) আইকনে ক্লিক করুন।'
          : 'To install this WebApp on your PC, click the "Install" or (+) icon in your Chrome/Edge browser address bar.'
      );
    }
  };

  const toggleFullscreen = () => {
    if (!document.fullscreenElement) {
      document.documentElement.requestFullscreen().catch(() => {});
    } else {
      document.exitFullscreen().catch(() => {});
    }
  };

  return (
    <header className="sticky top-0 z-40 w-full border-b border-white/10 backdrop-blur-xl bg-slate-950/70 transition-colors">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
        
        {/* Brand & Logo with Author Attribution */}
        <div className="flex items-center space-x-3 cursor-pointer group" onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}>
          <div className="relative w-12 h-12 rounded-2xl overflow-hidden p-[2px] transition-transform duration-300 group-hover:scale-105 shadow-lg shadow-purple-500/20"
               style={{ background: `linear-gradient(135deg, ${currentTheme.primaryAccent}, ${currentTheme.warmAccent})` }}>
            <img
              src="/logo.jpg"
              alt="File Bodlai Logo"
              className="w-full h-full object-cover rounded-xl"
              onError={(e) => {
                (e.target as HTMLElement).style.display = 'none';
              }}
            />
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <span className="font-extrabold text-xl sm:text-2xl tracking-tight bg-gradient-to-r from-white via-slate-100 to-slate-300 bg-clip-text text-transparent">
                {t.appTitle}
              </span>
              <span className="hidden sm:inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-white/10 text-white border border-white/15">
                PC WebApp
              </span>
            </div>
            <div className="flex items-center space-x-2">
              <span className="text-[11px] font-semibold text-purple-300/90 tracking-wide">
                Developed by S. M. Mahmud Iqbal
              </span>
            </div>
          </div>
        </div>

        {/* Center: Offline Privacy Pill Badge */}
        <div
          onClick={() => onOpenLegalModal('privacy')}
          className="hidden md:flex items-center space-x-2 px-3.5 py-1.5 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs font-medium backdrop-blur-md cursor-pointer hover:bg-emerald-500/15 transition-colors"
          title={t.offlineTooltip}
        >
          <ShieldCheck className="w-4 h-4 text-emerald-400 animate-pulse" />
          <span>{t.offlineBadge}</span>
        </div>

        {/* Right Actions */}
        <div className="flex items-center space-x-2 sm:space-x-3">
          
          {/* PC Install Button or Desktop Mode Pill */}
          {isStandalone ? (
            <div className="hidden sm:flex items-center space-x-1.5 px-3 py-1.5 rounded-xl bg-cyan-500/10 border border-cyan-500/30 text-cyan-300 text-xs font-semibold">
              <Laptop className="w-3.5 h-3.5" />
              <span>PC Mode Active</span>
            </div>
          ) : (
            <button
              onClick={handleInstallClick}
              className="flex items-center space-x-1.5 px-3 py-1.5 rounded-xl text-xs font-bold bg-gradient-to-r from-purple-500/20 to-pink-500/20 hover:from-purple-500/30 hover:to-pink-500/30 text-white border border-purple-500/30 transition-all hover:scale-105 active:scale-95 shadow-md shadow-purple-500/10"
              title="Install as Desktop App on PC / পিসিতে ইনস্টল করুন"
            >
              <MonitorDown className="w-3.5 h-3.5 text-purple-300" />
              <span>{language === 'bn' ? 'পিসিতে ইনস্টল' : 'Install PC App'}</span>
            </button>
          )}

          {/* Fullscreen PC Toggle */}
          <button
            onClick={toggleFullscreen}
            className="hidden lg:flex p-2 rounded-xl text-slate-300 bg-white/5 hover:bg-white/10 border border-white/10 transition-all hover:text-white"
            title={isFullscreen ? "Exit Fullscreen" : "Fullscreen PC View"}
          >
            {isFullscreen ? <Minimize className="w-4 h-4" /> : <Maximize className="w-4 h-4" />}
          </button>

          {/* Language Toggle */}
          <button
            onClick={() => onLanguageChange(language === 'en' ? 'bn' : 'en')}
            className="flex items-center space-x-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold bg-white/5 hover:bg-white/10 text-slate-200 border border-white/10 transition-all hover:scale-105 active:scale-95"
            title="Switch Language / ভাষা পরিবর্তন করুন (Ctrl+L)"
          >
            <Globe className="w-3.5 h-3.5 text-slate-400" />
            <span>{language === 'en' ? 'বাংলা' : 'English'}</span>
          </button>

          {/* Theme Selector */}
          <button
            onClick={onOpenThemeModal}
            className="flex items-center space-x-2 px-3 py-1.5 rounded-xl text-xs font-medium bg-white/5 hover:bg-white/10 text-slate-200 border border-white/10 transition-all hover:scale-105 active:scale-95"
            title="Customize Appearance & Themes (Ctrl+T)"
          >
            <span
              className="w-3 h-3 rounded-full shadow-sm"
              style={{ background: `linear-gradient(135deg, ${currentTheme.primaryAccent}, ${currentTheme.warmAccent})` }}
            />
            <span className="hidden sm:inline font-semibold">
              {language === 'bn' ? currentTheme.titleBn : currentTheme.title}
            </span>
            <Palette className="w-3.5 h-3.5 text-slate-400" />
          </button>

          {/* Android APK Link */}
          <a
            href="https://github.com/SMMahmudIqbal/FileBodlai/releases"
            target="_blank"
            rel="noopener noreferrer"
            className="hidden xl:flex items-center space-x-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold bg-purple-600/20 hover:bg-purple-600/30 text-purple-300 border border-purple-500/30 transition-all hover:scale-105"
            title="Download Android APK"
          >
            <Smartphone className="w-3.5 h-3.5 text-purple-400" />
            <span>{t.getAndroidApp}</span>
          </a>

          {/* GitHub Link */}
          <a
            href="https://github.com/SMMahmudIqbal/FileBodlai"
            target="_blank"
            rel="noopener noreferrer"
            className="p-2 rounded-xl text-slate-300 bg-white/5 hover:bg-white/10 border border-white/10 transition-all hover:text-white"
            title="View Source on GitHub"
          >
            <GithubIcon className="w-4 h-4" />
          </a>
        </div>

      </div>
    </header>
  );
};
