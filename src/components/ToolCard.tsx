import React from 'react';
import { Presentation, FileStack, FileImage, Images, Image, FileText, ArrowRight } from 'lucide-react';
import type { ToolMeta } from '../utils/translations';
import type { Language, ThemePreset } from '../types';

interface ToolCardProps {
  tool: ToolMeta;
  language: Language;
  currentTheme: ThemePreset;
  onSelect: (tool: ToolMeta) => void;
}

const ICON_MAP: Record<string, React.ElementType> = {
  Presentation,
  FileStack,
  FileImage,
  Images,
  Image,
  FileText,
};

export const ToolCard: React.FC<ToolCardProps> = ({
  tool,
  language,
  currentTheme,
  onSelect,
}) => {
  const IconComponent = ICON_MAP[tool.iconName] || FileText;

  return (
    <div
      onClick={() => onSelect(tool)}
      className="group relative rounded-3xl p-6 sm:p-7 transition-all duration-300 cursor-pointer overflow-hidden flex flex-col justify-between border border-white/10 hover:border-white/25 hover:-translate-y-1.5 shadow-xl shadow-black/30"
      style={{
        backgroundColor: currentTheme.surfaceCard,
        backdropFilter: 'blur(16px)',
      }}
    >
      {/* Dynamic Hover Glow */}
      <div
        className="absolute -right-16 -top-16 w-36 h-36 rounded-full blur-3xl opacity-0 group-hover:opacity-40 transition-opacity duration-500 pointer-events-none"
        style={{ backgroundColor: tool.accent }}
      />

      <div>
        {/* Top Header: Icon + Badge */}
        <div className="flex items-center justify-between mb-5">
          <div
            className="w-14 h-14 rounded-2xl flex items-center justify-center transition-transform duration-300 group-hover:scale-110 shadow-lg"
            style={{
              backgroundColor: `${tool.accent}20`,
              color: tool.accent,
              border: `1px solid ${tool.accent}40`,
            }}
          >
            <IconComponent className="w-7 h-7" />
          </div>

          <span
            className="px-3 py-1 rounded-full text-xs font-semibold border backdrop-blur-md"
            style={{
              backgroundColor: `${tool.accent}15`,
              color: tool.accent,
              borderColor: `${tool.accent}30`,
            }}
          >
            {language === 'bn' ? tool.badgeBn : tool.badgeEn}
          </span>
        </div>

        {/* Title */}
        <h3 className="text-xl font-bold text-white group-hover:text-transparent group-hover:bg-clip-text transition-colors duration-300"
            style={{
              backgroundImage: `linear-gradient(135deg, #FFFFFF, ${tool.accent})`
            }}>
          {language === 'bn' ? tool.nameBn : tool.nameEn}
        </h3>

        {/* Secondary Title (cross-lingual hint) */}
        <p className="text-xs font-medium text-slate-400 mt-0.5">
          {language === 'bn' ? tool.nameEn : tool.nameBn}
        </p>

        {/* Description */}
        <p className="text-sm text-slate-300 mt-3 leading-relaxed">
          {language === 'bn' ? tool.descBn : tool.descEn}
        </p>
      </div>

      {/* Bottom CTA */}
      <div className="mt-6 pt-4 border-t border-white/5 flex items-center justify-between text-xs font-semibold"
           style={{ color: tool.accent }}>
        <span>
          {language === 'bn' ? 'কনভার্টার খুলুন' : 'Open Converter'}
        </span>
        <div className="w-7 h-7 rounded-xl flex items-center justify-center bg-white/5 group-hover:bg-white/10 group-hover:translate-x-1 transition-all">
          <ArrowRight className="w-4 h-4" />
        </div>
      </div>
    </div>
  );
};
