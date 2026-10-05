import React from 'react';
import { Palette, ShieldCheck, Globe, Smartphone } from 'lucide-react';
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

  return (
    <header className="sticky top-0 z-40 w-full border-b border-white/10 backdrop-blur-xl bg-slate-950/60 transition-colors">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
        
        {/* Brand & Logo */}
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
              <span className="hidden sm:inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold bg-white/10 text-white border border-white/15">
                v1.0.0
              </span>
            </div>
            <p className="text-xs text-slate-400 font-medium tracking-wide">
              {language === 'bn' ? 'ফাইল বদলাই • ১০০% অফলাইন' : 'Private Offline File Studio'}
            </p>
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
          
          {/* Language Toggle */}
          <button
            onClick={() => onLanguageChange(language === 'en' ? 'bn' : 'en')}
            className="flex items-center space-x-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold bg-white/5 hover:bg-white/10 text-slate-200 border border-white/10 transition-all hover:scale-105 active:scale-95"
            title="Switch Language / ভাষা পরিবর্তন করুন"
          >
            <Globe className="w-3.5 h-3.5 text-slate-400" />
            <span>{language === 'en' ? 'বাংলা' : 'English'}</span>
          </button>

          {/* Theme Selector */}
          <button
            onClick={onOpenThemeModal}
            className="flex items-center space-x-2 px-3 py-1.5 rounded-xl text-xs font-medium bg-white/5 hover:bg-white/10 text-slate-200 border border-white/10 transition-all hover:scale-105 active:scale-95"
            title="Customize Appearance & Themes"
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
            className="hidden lg:flex items-center space-x-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold bg-purple-600/20 hover:bg-purple-600/30 text-purple-300 border border-purple-500/30 transition-all hover:scale-105"
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
