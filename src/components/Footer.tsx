import React from 'react';
import { ShieldCheck, Smartphone } from 'lucide-react';
import type { Language, ThemePreset } from '../types';
import { UI_STRINGS } from '../utils/translations';
import { GithubIcon } from './GithubIcon';

interface FooterProps {
  language: Language;
  currentTheme: ThemePreset;
  onOpenLegalModal: (tab: 'privacy' | 'terms' | 'about') => void;
}

export const Footer: React.FC<FooterProps> = ({
  language,
  onOpenLegalModal,
}) => {
  const t = UI_STRINGS[language];

  return (
    <footer className="w-full border-t border-white/10 mt-20 bg-slate-950/80 backdrop-blur-xl">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 items-center text-center md:text-left">
          
          {/* Col 1: Brand Info */}
          <div>
            <div className="flex items-center justify-center md:justify-start space-x-2.5">
              <img src="/logo.jpg" alt="Logo" className="w-8 h-8 rounded-lg object-cover" />
              <span className="font-extrabold text-lg text-white">
                {t.appTitle}
              </span>
            </div>
            <p className="text-xs text-slate-400 mt-2 max-w-sm">
              {language === 'bn'
                ? '১০০% অফলাইন ফাইল কনভার্টার। কোনো সার্ভার নেই, কোনো ট্র্যাকিং নেই।'
                : '100% offline and private file converter. Zero server uploads, zero tracking.'}
            </p>
          </div>

          {/* Col 2: Navigation Links */}
          <div className="flex flex-wrap items-center justify-center gap-4 text-xs font-semibold text-slate-300">
            <button
              onClick={() => onOpenLegalModal('privacy')}
              className="hover:text-white transition-colors"
            >
              {t.privacyTab}
            </button>
            <span className="text-slate-600">•</span>
            <button
              onClick={() => onOpenLegalModal('terms')}
              className="hover:text-white transition-colors"
            >
              {t.termsTab}
            </button>
            <span className="text-slate-600">•</span>
            <button
              onClick={() => onOpenLegalModal('about')}
              className="hover:text-white transition-colors"
            >
              {t.aboutTab}
            </button>
          </div>

          {/* Col 3: External Links */}
          <div className="flex items-center justify-center md:justify-end space-x-3">
            <a
              href="https://github.com/SMMahmudIqbal/FileBodlai"
              target="_blank"
              rel="noopener noreferrer"
              className="p-2.5 rounded-xl bg-white/5 hover:bg-white/10 text-slate-300 hover:text-white border border-white/10 transition-all"
              title="GitHub Repository"
            >
              <GithubIcon className="w-4 h-4" />
            </a>

            <a
              href="https://github.com/SMMahmudIqbal/FileBodlai/releases"
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center space-x-1.5 px-3 py-2 rounded-xl bg-purple-600/20 hover:bg-purple-600/30 text-purple-300 border border-purple-500/30 text-xs font-semibold transition-all"
            >
              <Smartphone className="w-3.5 h-3.5" />
              <span>Android APK</span>
            </a>
          </div>

        </div>

        {/* Bottom Bar */}
        <div className="mt-8 pt-8 border-t border-white/5 flex flex-col sm:flex-row items-center justify-between text-[11px] text-slate-500 gap-3">
          <p>
            © {new Date().getFullYear()} File Bodlai. {t.author}.
          </p>
          <div className="flex items-center space-x-2 text-emerald-400">
            <ShieldCheck className="w-3.5 h-3.5" />
            <span>Air-Gapped Client Memory Engine</span>
          </div>
        </div>

      </div>
    </footer>
  );
};
