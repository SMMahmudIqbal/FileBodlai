export type ToolId = 
  | 'pdf-to-pptx'
  | 'merge-pdf'
  | 'image-to-pdf'
  | 'pdf-to-images'
  | 'image-to-image'
  | 'text-to-pdf';

export type Language = 'en' | 'bn';

export interface ThemePreset {
  id: string;
  title: string;
  titleBn: string;
  category: 'Dark' | 'Day' | 'Art';
  primaryAccent: string;
  warmAccent: string;
  bgTop: string;
  bgMid: string;
  bgBottom: string;
  isDark: boolean;
  surfaceCard: string;
  surfaceBorder: string;
  surfaceSubtle: string;
  textPrimary: string;
  textSecondary: string;
  cardGlow: string;
}

export interface FileItem {
  id: string;
  file: File;
  name: string;
  size: number;
  previewUrl?: string;
  pageCount?: number;
}
