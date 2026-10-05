import React from 'react';
import { X, Palette, Check } from 'lucide-react';
import { THEME_PRESETS } from '../utils/themePresets';
import type { Language, ThemePreset } from '../types';

interface ThemeModalProps {
  isOpen: boolean;
  onClose: () => void;
  currentTheme: ThemePreset;
  onSelectTheme: (theme: ThemePreset) => void;
  language: Language;
}

export const ThemeModal: React.FC<ThemeModalProps> = ({
  isOpen,
  onClose,
  currentTheme,
  onSelectTheme,
  language,
}) => {
  if (!isOpen) return null;

  const categories: Array<{ id: 'Dark' | 'Day' | 'Art'; nameEn: string; nameBn: string }> = [
    { id: 'Dark', nameEn: 'Nocturnal Dark', nameBn: 'নকটার্নাল ডার্ক' },
    { id: 'Day', nameEn: 'Elegant Day', nameBn: 'এলিগেন্ট ডে' },
    { id: 'Art', nameEn: 'Art & Avant-Garde', nameBn: 'আর্ট ও আভঁ-গার্দ' },
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-6 overflow-y-auto bg-black/80 backdrop-blur-md animate-in fade-in duration-200">
      <div
        className="relative w-full max-w-4xl rounded-3xl overflow-hidden border border-white/15 shadow-2xl transition-all"
        style={{
          backgroundColor: currentTheme.surfaceCard,
          backdropFilter: 'blur(24px)',
        }}
      >
        {/* Header */}
        <div className="flex items-center justify-between p-5 sm:p-6 border-b border-white/10 bg-white/5">
          <div className="flex items-center space-x-3">
            <div className="p-2.5 rounded-xl bg-purple-500/20 text-purple-300 border border-purple-500/30">
              <Palette className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-white">
                {language === 'bn' ? '১৯টি ইউনিক থিম বেছে নিন' : 'Customize App Theme (19 Presets)'}
              </h2>
              <p className="text-xs text-slate-400">
                {language === 'bn' ? 'অ্যান্ড্রয়েড অ্যাপের সাথে শতভাগ মিল রাখা থিম কালেকশন' : 'Directly inspired by the official Android App palette'}
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

        {/* Themes Grid */}
        <div className="p-5 sm:p-7 max-h-[75vh] overflow-y-auto space-y-7">
          {categories.map((cat) => {
            const list = THEME_PRESETS.filter((t) => t.category === cat.id);
            return (
              <div key={cat.id}>
                <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-3 px-1">
                  {language === 'bn' ? cat.nameBn : cat.nameEn} ({list.length})
                </h3>

                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
                  {list.map((theme) => {
                    const isSelected = currentTheme.id === theme.id;
                    return (
                      <button
                        key={theme.id}
                        onClick={() => {
                          onSelectTheme(theme);
                        }}
                        className={`group relative p-3.5 rounded-2xl border text-left transition-all duration-200 overflow-hidden ${
                          isSelected
                            ? 'border-white shadow-lg scale-[1.02]'
                            : 'border-white/10 hover:border-white/30 hover:scale-[1.01]'
                        }`}
                        style={{
                          background: `linear-gradient(135deg, ${theme.bgTop}, ${theme.bgMid})`,
                        }}
                      >
                        {/* Selected Indicator */}
                        {isSelected && (
                          <div className="absolute top-2 right-2 w-5 h-5 rounded-full bg-white text-black flex items-center justify-center shadow-md">
                            <Check className="w-3 h-3 stroke-[3]" />
                          </div>
                        )}

                        {/* Color Swatch Dots */}
                        <div className="flex items-center space-x-1.5 mb-2.5">
                          <span
                            className="w-4 h-4 rounded-full shadow-sm"
                            style={{ backgroundColor: theme.primaryAccent }}
                          />
                          <span
                            className="w-4 h-4 rounded-full shadow-sm"
                            style={{ backgroundColor: theme.warmAccent }}
                          />
                          <span
                            className="w-4 h-4 rounded-full shadow-sm border border-white/20"
                            style={{ backgroundColor: theme.bgBottom }}
                          />
                        </div>

                        {/* Title */}
                        <p className="text-xs font-bold truncate text-white">
                          {language === 'bn' ? theme.titleBn : theme.title}
                        </p>
                        <p className="text-[10px] text-slate-400 truncate mt-0.5">
                          {language === 'bn' ? theme.title : theme.titleBn}
                        </p>
                      </button>
                    );
                  })}
                </div>
              </div>
            );
          })}
        </div>

        {/* Footer */}
        <div className="p-4 sm:p-5 border-t border-white/10 bg-white/5 flex justify-end">
          <button
            onClick={onClose}
            className="px-6 py-2.5 rounded-xl font-bold text-xs bg-white text-black hover:bg-slate-200 transition-all"
          >
            {language === 'bn' ? 'সম্পন্ন' : 'Done'}
          </button>
        </div>

      </div>
    </div>
  );
};
