import React, { useState } from 'react';
import { X, ShieldCheck, FileCheck, Info, ExternalLink, Smartphone, Heart } from 'lucide-react';
import type { Language, ThemePreset } from '../types';
import { UI_STRINGS } from '../utils/translations';
import { GithubIcon } from './GithubIcon';

interface LegalModalProps {
  isOpen: boolean;
  initialTab?: 'privacy' | 'terms' | 'about';
  onClose: () => void;
  language: Language;
  currentTheme: ThemePreset;
}

export const LegalModal: React.FC<LegalModalProps> = ({
  isOpen,
  initialTab = 'privacy',
  onClose,
  language,
  currentTheme,
}) => {
  if (!isOpen) return null;
  const t = UI_STRINGS[language];
  const [activeTab, setActiveTab] = useState<'privacy' | 'terms' | 'about'>(initialTab);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-6 overflow-y-auto bg-black/80 backdrop-blur-md animate-in fade-in duration-200">
      <div
        className="relative w-full max-w-3xl rounded-3xl overflow-hidden border border-white/15 shadow-2xl transition-all"
        style={{
          backgroundColor: currentTheme.surfaceCard,
          backdropFilter: 'blur(24px)',
        }}
      >
        {/* Top Header */}
        <div className="flex items-center justify-between p-5 sm:p-6 border-b border-white/10 bg-white/5">
          <div className="flex items-center space-x-3">
            <div className="p-2.5 rounded-xl bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-white">
                {t.privacyModalTitle}
              </h2>
              <p className="text-xs text-slate-400">
                {t.privacyModalDesc}
              </p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="p-2 rounded-xl text-slate-400 hover:text-white bg-white/5 hover:bg-white/10 border border-white/10 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Tab Navigation */}
        <div className="flex border-b border-white/10 px-6 pt-3 bg-white/5 space-x-4">
          <button
            onClick={() => setActiveTab('privacy')}
            className={`pb-3 text-xs font-bold transition-colors border-b-2 flex items-center space-x-2 ${
              activeTab === 'privacy'
                ? 'border-emerald-400 text-white'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <ShieldCheck className="w-4 h-4" />
            <span>{t.privacyTab}</span>
          </button>

          <button
            onClick={() => setActiveTab('terms')}
            className={`pb-3 text-xs font-bold transition-colors border-b-2 flex items-center space-x-2 ${
              activeTab === 'terms'
                ? 'border-purple-400 text-white'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <FileCheck className="w-4 h-4" />
            <span>{t.termsTab}</span>
          </button>

          <button
            onClick={() => setActiveTab('about')}
            className={`pb-3 text-xs font-bold transition-colors border-b-2 flex items-center space-x-2 ${
              activeTab === 'about'
                ? 'border-cyan-400 text-white'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <Info className="w-4 h-4" />
            <span>{t.aboutTab}</span>
          </button>
        </div>

        {/* Content Body */}
        <div className="p-6 sm:p-8 max-h-[65vh] overflow-y-auto text-xs sm:text-sm text-slate-300 leading-relaxed space-y-4">
          
          {/* TAB 1: Privacy Policy */}
          {activeTab === 'privacy' && (
            <div className="space-y-4">
              <div className="p-4 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-300 text-xs">
                <strong>{language === 'bn' ? '১০০% অফলাইন গ্যারান্টি:' : '100% Offline Air-Gapped Guarantee:'}</strong>{' '}
                {language === 'bn'
                  ? 'আপনার কোনো ফাইল কোনো সার্ভারে আপলোড করা হয় না। রূপান্তর প্রক্রিয়া সরাসরি আপনার ব্রাউজারের মেমরিতে সম্পন্ন হয়।'
                  : 'Your files are processed directly inside client-side browser memory. Nothing is ever uploaded to any server or cloud API.'}
              </div>

              <h4 className="text-white font-bold text-sm">1. Zero Server Uploads</h4>
              <p>
                Unlike standard cloud file converters that upload your confidential files to remote servers, File Bodlai (ফাইল বদলাই) operates on a 100% client-side architecture using standard HTML5 Canvas, WebAssembly, and local JavaScript libraries.
              </p>

              <h4 className="text-white font-bold text-sm">2. Zero Analytics & Tracking</h4>
              <p>
                We do not use Google Analytics, cookies, IP loggers, or tracking scripts. Your session is completely anonymous.
              </p>

              <h4 className="text-white font-bold text-sm">3. Local Storage</h4>
              <p>
                We only store your theme and language preferences locally in your browser's localStorage. No personal identification data is ever gathered.
              </p>
            </div>
          )}

          {/* TAB 2: Terms of Service */}
          {activeTab === 'terms' && (
            <div className="space-y-4">
              <h4 className="text-white font-bold text-sm">MIT Open Source License</h4>
              <p>
                File Bodlai is open source and distributed under the permissive MIT License. You are free to use this application for personal, academic, or commercial purposes.
              </p>

              <h4 className="text-white font-bold text-sm">No Warranty</h4>
              <p>
                THE SOFTWARE IS PROVIDED &quot;AS IS&quot;, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
              </p>

              <h4 className="text-white font-bold text-sm">Client Responsibility</h4>
              <p>
                Always verify your output documents after conversion before deleting your original source files.
              </p>
            </div>
          )}

          {/* TAB 3: About File Bodlai */}
          {activeTab === 'about' && (
            <div className="space-y-5">
              <div className="flex items-center space-x-3 p-4 rounded-2xl bg-white/5 border border-white/10">
                <img src="/logo.jpg" alt="Logo" className="w-12 h-12 rounded-xl object-cover" />
                <div>
                  <h4 className="text-white font-bold text-base">File Bodlai (ফাইল বদলাই)</h4>
                  <p className="text-xs text-slate-400">Version 1.0.0 • Open Source Project</p>
                </div>
              </div>

              <p>
                File Bodlai was <strong>Developed by S. M. Mahmud Iqbal</strong> to empower users with a fast, private, and beautifully designed alternative to bloated online file converters.
              </p>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
                <a
                  href="https://github.com/SMMahmudIqbal/FileBodlai"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center justify-between p-3.5 rounded-2xl bg-white/5 hover:bg-white/10 border border-white/10 text-white transition-all group"
                >
                  <div className="flex items-center space-x-2.5">
                    <GithubIcon className="w-4 h-4 text-purple-400" />
                    <span className="font-semibold text-xs">GitHub Repository</span>
                  </div>
                  <ExternalLink className="w-3.5 h-3.5 text-slate-400 group-hover:text-white" />
                </a>

                <a
                  href="https://github.com/SMMahmudIqbal/FileBodlai/releases"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center justify-between p-3.5 rounded-2xl bg-purple-600/20 hover:bg-purple-600/30 border border-purple-500/30 text-purple-200 transition-all group"
                >
                  <div className="flex items-center space-x-2.5">
                    <Smartphone className="w-4 h-4 text-purple-400" />
                    <span className="font-semibold text-xs">Download Android APK</span>
                  </div>
                  <ExternalLink className="w-3.5 h-3.5 text-purple-400 group-hover:text-purple-200" />
                </a>
              </div>

              <div className="pt-2 text-slate-400 text-xs flex items-center justify-center space-x-1">
                <span>Designed & engineered with</span>
                <Heart className="w-3.5 h-3.5 text-red-500 fill-red-500 inline" />
                <span>for the open web.</span>
              </div>
            </div>
          )}

        </div>

        {/* Footer */}
        <div className="p-4 sm:p-5 border-t border-white/10 bg-white/5 flex justify-end">
          <button
            onClick={onClose}
            className="px-6 py-2.5 rounded-xl font-bold text-xs bg-white text-black hover:bg-slate-200 transition-all"
          >
            {t.close}
          </button>
        </div>

      </div>
    </div>
  );
};
