import React from 'react';
import { ShieldCheck, Zap, Lock, Sparkles } from 'lucide-react';
import type { Language, ThemePreset } from '../types';
import { UI_STRINGS } from '../utils/translations';

interface HeroProps {
  language: Language;
  currentTheme: ThemePreset;
  onOpenLegalModal: () => void;
}

export const Hero: React.FC<HeroProps> = ({ language, currentTheme, onOpenLegalModal }) => {
  const t = UI_STRINGS[language];

  return (
    <section className="relative pt-12 pb-16 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto text-center overflow-hidden">
      
      {/* Ambient background glow */}
      <div
        className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[300px] blur-[140px] opacity-25 pointer-events-none rounded-full"
        style={{
          background: `radial-gradient(circle, ${currentTheme.primaryAccent} 0%, ${currentTheme.warmAccent} 100%)`
        }}
      />

      {/* Pill Tag */}
      <div className="inline-flex items-center space-x-2 px-4 py-1.5 rounded-full bg-white/5 border border-white/10 text-xs sm:text-sm font-medium text-slate-300 backdrop-blur-md mb-6 hover:border-white/20 transition-all cursor-pointer"
           onClick={onOpenLegalModal}>
        <Sparkles className="w-4 h-4 text-amber-400" />
        <span>{t.heroTag}</span>
      </div>

      {/* Hero Headline */}
      <h1 className="text-3xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-white max-w-4xl mx-auto leading-[1.15] mb-6">
        {language === 'bn' ? (
          <>
            সম্পূর্ণ নিরাপত্তায় <span className="bg-gradient-to-r from-purple-400 via-pink-400 to-amber-300 bg-clip-text text-transparent">ফাইল কনভার্ট ও মার্জ</span> করুন
          </>
        ) : (
          <>
            Convert, Merge & Transform Files With{' '}
            <span
              className="bg-clip-text text-transparent"
              style={{
                backgroundImage: `linear-gradient(135deg, ${currentTheme.primaryAccent}, ${currentTheme.warmAccent})`
              }}
            >
              Absolute Privacy
            </span>
          </>
        )}
      </h1>

      {/* Subtitle */}
      <p className="text-base sm:text-lg text-slate-300 max-w-2xl mx-auto leading-relaxed mb-10">
        {t.heroDesc}
      </p>

      {/* Value Badges */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 max-w-3xl mx-auto mb-10 text-left">
        <div className="p-4 rounded-2xl bg-white/5 border border-white/10 backdrop-blur-md flex items-start space-x-3.5">
          <div className="p-2.5 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 shrink-0">
            <Lock className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-sm font-semibold text-white">
              {language === 'bn' ? '১০০% অফলাইন প্রসেসিং' : '100% Local Execution'}
            </h2>
            <p className="text-xs text-slate-400 mt-0.5">
              {language === 'bn' ? 'ফাইল মেমরিতে প্রসেস হয়, সার্ভারে নয়।' : 'Files never touch any external server.'}
            </p>
          </div>
        </div>

        <div className="p-4 rounded-2xl bg-white/5 border border-white/10 backdrop-blur-md flex items-start space-x-3.5">
          <div className="p-2.5 rounded-xl bg-cyan-500/10 text-cyan-400 border border-cyan-500/20 shrink-0">
            <Zap className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-sm font-semibold text-white">
              {language === 'bn' ? 'তাত্ক্ষণিক স্পিড' : 'Zero Upload Wait'}
            </h2>
            <p className="text-xs text-slate-400 mt-0.5">
              {language === 'bn' ? 'কোনো ধীরগতির নেটওয়ার্ক আপলোড নেই।' : 'Direct RAM conversion at hardware speed.'}
            </p>
          </div>
        </div>

        <div className="p-4 rounded-2xl bg-white/5 border border-white/10 backdrop-blur-md flex items-start space-x-3.5">
          <div className="p-2.5 rounded-xl bg-purple-500/10 text-purple-400 border border-purple-500/20 shrink-0">
            <ShieldCheck className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-sm font-semibold text-white">
              {language === 'bn' ? 'জিরো ট্র্যাকিং বা লগ' : 'Zero Telemetry'}
            </h2>
            <p className="text-xs text-slate-400 mt-0.5">
              {language === 'bn' ? 'কোনো কুকিজ, এনালিটিক্স বা স্টোরেজ নেই।' : 'No analytics, cookies, or tracking.'}
            </p>
          </div>
        </div>
      </div>

    </section>
  );
};
