import React, { useState, useRef, useEffect } from 'react';
import {
  X, UploadCloud, FileText, Trash2, ArrowUp, ArrowDown,
  Download, CheckCircle2, AlertCircle, RefreshCw, Sparkles, Sliders
} from 'lucide-react';
import confetti from 'canvas-confetti';
import type { ToolMeta } from '../utils/translations';
import { UI_STRINGS } from '../utils/translations';
import type { Language, ThemePreset } from '../types';
import { convertPdfToPptx } from '../converters/pdfToPptx';
import { mergePdfFiles } from '../converters/mergePdf';
import type { ImageToPdfOptions } from '../converters/imageToPdf';
import { convertImagesToPdf } from '../converters/imageToPdf';
import type { ExtractedImage } from '../converters/pdfToImages';
import { convertPdfToImages, createZipFromImages } from '../converters/pdfToImages';
import type { ImageConvertOptions } from '../converters/imageToImage';
import { convertImage } from '../converters/imageToImage';
import type { TextToPdfOptions } from '../converters/textToPdf';
import { convertTextToPdf } from '../converters/textToPdf';

interface ToolModalProps {
  tool: ToolMeta | null;
  language: Language;
  currentTheme: ThemePreset;
  onClose: () => void;
}

export const ToolModal: React.FC<ToolModalProps> = ({
  tool,
  language,
  currentTheme,
  onClose,
}) => {
  if (!tool) return null;
  const t = UI_STRINGS[language];

  // State
  const [files, setFiles] = useState<File[]>([]);
  const [isDragging, setIsDragging] = useState(false);
  const [status, setStatus] = useState<'idle' | 'converting' | 'success' | 'error'>('idle');
  const [progressMsg, setProgressMsg] = useState('');
  const [progressPercent, setProgressPercent] = useState(0);
  const [errorMessage, setErrorMessage] = useState('');
  const [resultBlob, setResultBlob] = useState<Blob | null>(null);
  const [resultFileName, setResultFileName] = useState('');
  const [extractedImages, setExtractedImages] = useState<ExtractedImage[]>([]);

  // Text-to-PDF text state
  const [textContent, setTextContent] = useState('');

  // Tool Specific Options • 1. Image to PDF options
  const [imgToPdfOpts, setImgToPdfOpts] = useState<ImageToPdfOptions>({
    pageSize: 'A4',
    orientation: 'auto',
    margin: 20,
  });

  // 2. PDF to Images options
  const [pdfToImgOpts, setPdfToImgOpts] = useState<{
    format: 'image/png' | 'image/jpeg';
    scale: number;
  }>({
    format: 'image/png',
    scale: 2.0,
  });

  // 3. Image to Image options
  const [imgToImgOpts, setImgToImgOpts] = useState<ImageConvertOptions>({
    targetFormat: 'image/webp',
    quality: 0.85,
    resizeMode: 'original',
    customWidth: 1200,
    customHeight: 800,
    maintainAspect: true,
  });

  // 4. Text to PDF options
  const [textToPdfOpts, setTextToPdfOpts] = useState<TextToPdfOptions>({
    fontSize: 12,
    lineHeightMultiplier: 1.5,
    pageSize: 'A4',
    addPageNumbers: true,
    documentTitle: 'Document',
  });

  const fileInputRef = useRef<HTMLInputElement>(null);

  // Reset when tool changes
  useEffect(() => {
    setFiles([]);
    setStatus('idle');
    setErrorMessage('');
    setResultBlob(null);
    setExtractedImages([]);
    setTextContent('');
  }, [tool]);

  // Handle Drag & Drop
  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = () => {
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      handleFilesSelected(Array.from(e.dataTransfer.files));
    }
  };

  const handleFileInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      handleFilesSelected(Array.from(e.target.files));
    }
  };

  const handleFilesSelected = (selected: File[]) => {
    if (tool.id === 'text-to-pdf') {
      const textFile = selected[0];
      if (textFile) {
        const reader = new FileReader();
        reader.onload = (ev) => {
          setTextContent((ev.target?.result as string) || '');
          setTextToPdfOpts(prev => ({
            ...prev,
            documentTitle: textFile.name.replace(/\.[^/.]+$/, '')
          }));
        };
        reader.readAsText(textFile);
        setFiles([textFile]);
      }
      return;
    }

    if (tool.multiple) {
      setFiles((prev) => [...prev, ...selected]);
    } else {
      setFiles([selected[0]]);
    }
    setStatus('idle');
    setErrorMessage('');
  };

  const removeFile = (index: number) => {
    setFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const moveFile = (index: number, direction: 'up' | 'down') => {
    setFiles((prev) => {
      const copy = [...prev];
      const targetIndex = direction === 'up' ? index - 1 : index + 1;
      if (targetIndex < 0 || targetIndex >= copy.length) return copy;
      const temp = copy[index];
      copy[index] = copy[targetIndex];
      copy[targetIndex] = temp;
      return copy;
    });
  };

  // Trigger Conversion
  const handleConvert = async () => {
    if (tool.id === 'text-to-pdf' && !textContent.trim()) {
      setErrorMessage(language === 'bn' ? 'অনুগ্রহ করে টেক্সট লিখুন বা .txt ফাইল সিলেক্ট করুন' : 'Please provide text or select a .txt file');
      return;
    } else if (tool.id !== 'text-to-pdf' && files.length === 0) {
      setErrorMessage(language === 'bn' ? 'অনুগ্রহ করে ফাইল যোগ করুন' : 'Please select at least one file');
      return;
    }

    setStatus('converting');
    setProgressPercent(10);
    setProgressMsg(language === 'bn' ? 'প্রক্রিয়াকরণ শুরু হচ্ছে...' : 'Initializing conversion...');
    setErrorMessage('');

    try {
      if (tool.id === 'pdf-to-pptx') {
        const outBlob = await convertPdfToPptx(files[0], (curr, total) => {
          setProgressPercent(Math.round((curr / total) * 90));
          setProgressMsg(
            language === 'bn'
              ? `স্লাইড তৈরি হচ্ছে (${curr}/${total})...`
              : `Converting slide (${curr}/${total})...`
          );
        });
        const outName = files[0].name.replace(/\.[^/.]+$/, '') + '.pptx';
        setResultBlob(outBlob);
        setResultFileName(outName);
      } else if (tool.id === 'merge-pdf') {
        const outBlob = await mergePdfFiles(files, (curr, total) => {
          setProgressPercent(Math.round((curr / total) * 90));
          setProgressMsg(
            language === 'bn'
              ? `ডকুমেন্ট মার্জ হচ্ছে (${curr}/${total})...`
              : `Merging document (${curr}/${total})...`
          );
        });
        setResultBlob(outBlob);
        setResultFileName('FileBodlai_Merged.pdf');
      } else if (tool.id === 'image-to-pdf') {
        const outBlob = await convertImagesToPdf(files, imgToPdfOpts, (curr, total) => {
          setProgressPercent(Math.round((curr / total) * 90));
          setProgressMsg(
            language === 'bn'
              ? `ছবি যোগ হচ্ছে (${curr}/${total})...`
              : `Processing image (${curr}/${total})...`
          );
        });
        setResultBlob(outBlob);
        setResultFileName('FileBodlai_Images.pdf');
      } else if (tool.id === 'pdf-to-images') {
        const images = await convertPdfToImages(files[0], pdfToImgOpts, (curr, total) => {
          setProgressPercent(Math.round((curr / total) * 90));
          setProgressMsg(
            language === 'bn'
              ? `পাতা রূপান্তর হচ্ছে (${curr}/${total})...`
              : `Extracting page (${curr}/${total})...`
          );
        });
        setExtractedImages(images);
        const zipBlob = await createZipFromImages(images, `${files[0].name.replace(/\.[^/.]+$/, '')}_pages.zip`);
        setResultBlob(zipBlob);
        setResultFileName(`${files[0].name.replace(/\.[^/.]+$/, '')}_pages.zip`);
      } else if (tool.id === 'image-to-image') {
        const res = await convertImage(files[0], imgToImgOpts);
        setProgressPercent(100);
        setResultBlob(res.blob);
        setResultFileName(res.name);
      } else if (tool.id === 'text-to-pdf') {
        setProgressPercent(50);
        setProgressMsg(language === 'bn' ? 'পিডিএফ ফরম্যাটিং করা হচ্ছে...' : 'Formatting PDF pages...');
        const outBlob = await convertTextToPdf(textContent, textToPdfOpts);
        setResultBlob(outBlob);
        const name = (textToPdfOpts.documentTitle || 'Document').replace(/\s+/g, '_') + '.pdf';
        setResultFileName(name);
      }

      setProgressPercent(100);
      setStatus('success');
      confetti({
        particleCount: 90,
        spread: 70,
        origin: { y: 0.6 },
        colors: [currentTheme.primaryAccent, currentTheme.warmAccent, '#10B981', '#38BDF8']
      });
    } catch (err: any) {
      console.error(err);
      setStatus('error');
      setErrorMessage(err.message || 'An unexpected error occurred during conversion');
    }
  };

  const handleDownload = (blob: Blob, fileName: string) => {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

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
            <div
              className="w-10 h-10 rounded-xl flex items-center justify-center font-bold text-lg"
              style={{
                backgroundColor: `${tool.accent}25`,
                color: tool.accent,
                border: `1px solid ${tool.accent}40`,
              }}
            >
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-white">
                {language === 'bn' ? tool.nameBn : tool.nameEn}
              </h2>
              <p className="text-xs text-slate-400">
                {language === 'bn' ? tool.nameEn : tool.nameBn} • 100% Client-Side
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

        {/* Content Body */}
        <div className="p-5 sm:p-7 max-h-[75vh] overflow-y-auto space-y-6">
          
          {/* File Upload Zone */}
          {status !== 'success' && (
            <div>
              {tool.id === 'text-to-pdf' ? (
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <label className="text-sm font-semibold text-slate-200">
                      {language === 'bn' ? 'টেক্সট লিখুন বা পেস্ট করুন (.txt ফাইল ও সাপোর্ট করে)' : 'Type or Paste Text (or select .txt file)'}
                    </label>
                    <button
                      onClick={() => fileInputRef.current?.click()}
                      className="text-xs text-purple-400 hover:text-purple-300 underline"
                    >
                      {language === 'bn' ? '.txt ফাইল আপলোড করুন' : 'Upload .txt file'}
                    </button>
                  </div>
                  <textarea
                    rows={6}
                    value={textContent}
                    onChange={(e) => setTextContent(e.target.value)}
                    placeholder={t.textInputPlaceholder}
                    className="w-full rounded-2xl p-4 bg-black/40 border border-white/10 text-white placeholder-slate-500 focus:outline-none focus:border-purple-500 transition-colors text-sm font-mono"
                  />
                  <input
                    ref={fileInputRef}
                    type="file"
                    accept=".txt,text/plain"
                    className="hidden"
                    onChange={handleFileInputChange}
                  />
                </div>
              ) : (
                <>
                  <div
                    onDragOver={handleDragOver}
                    onDragLeave={handleDragLeave}
                    onDrop={handleDrop}
                    onClick={() => fileInputRef.current?.click()}
                    className={`border-2 border-dashed rounded-3xl p-8 text-center cursor-pointer transition-all duration-300 flex flex-col items-center justify-center ${
                      isDragging
                        ? 'border-purple-400 bg-purple-500/10 scale-[0.99]'
                        : 'border-white/20 bg-white/5 hover:bg-white/10 hover:border-white/30'
                    }`}
                  >
                    <div
                      className="w-16 h-16 rounded-2xl flex items-center justify-center mb-4 transition-transform group-hover:scale-110 shadow-lg"
                      style={{
                        backgroundColor: `${tool.accent}20`,
                        color: tool.accent,
                        border: `1px solid ${tool.accent}40`,
                      }}
                    >
                      <UploadCloud className="w-8 h-8" />
                    </div>
                    <p className="text-base font-semibold text-white">
                      {t.dropzoneTitle}
                    </p>
                    <p className="text-xs text-slate-400 mt-1">
                      {t.dropzoneSubtitle} ({tool.multiple ? 'Multiple files allowed' : 'Single file'})
                    </p>
                    <input
                      ref={fileInputRef}
                      type="file"
                      accept={tool.accept}
                      multiple={tool.multiple}
                      className="hidden"
                      onChange={handleFileInputChange}
                    />
                  </div>
                </>
              )}

              {/* Selected Files List */}
              {files.length > 0 && tool.id !== 'text-to-pdf' && (
                <div className="mt-5 space-y-2">
                  <div className="flex items-center justify-between text-xs font-semibold text-slate-300 px-1">
                    <span>
                      {t.filesSelected} ({files.length})
                    </span>
                    {files.length > 1 && (
                      <button
                        onClick={() => setFiles([])}
                        className="text-red-400 hover:text-red-300 transition-colors"
                      >
                        {t.clearAll}
                      </button>
                    )}
                  </div>

                  <div className="max-h-48 overflow-y-auto space-y-2 pr-1">
                    {files.map((file, idx) => (
                      <div
                        key={idx}
                        className="flex items-center justify-between p-3 rounded-2xl bg-white/5 border border-white/10 text-xs text-white"
                      >
                        <div className="flex items-center space-x-3 overflow-hidden">
                          <FileText className="w-4 h-4 text-purple-400 shrink-0" />
                          <span className="truncate font-medium">{file.name}</span>
                          <span className="text-slate-400 shrink-0">
                            ({(file.size / 1024).toFixed(1)} KB)
                          </span>
                        </div>

                        <div className="flex items-center space-x-1 shrink-0">
                          {tool.multiple && (
                            <>
                              <button
                                disabled={idx === 0}
                                onClick={(e) => { e.stopPropagation(); moveFile(idx, 'up'); }}
                                className="p-1 hover:text-purple-400 disabled:opacity-30"
                              >
                                <ArrowUp className="w-3.5 h-3.5" />
                              </button>
                              <button
                                disabled={idx === files.length - 1}
                                onClick={(e) => { e.stopPropagation(); moveFile(idx, 'down'); }}
                                className="p-1 hover:text-purple-400 disabled:opacity-30"
                              >
                                <ArrowDown className="w-3.5 h-3.5" />
                              </button>
                            </>
                          )}
                          <button
                            onClick={(e) => { e.stopPropagation(); removeFile(idx); }}
                            className="p-1 text-slate-400 hover:text-red-400 ml-1"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Options Section */}
          {status !== 'success' && (
            <div className="p-5 rounded-2xl bg-black/30 border border-white/10 space-y-4">
              <div className="flex items-center space-x-2 text-xs font-bold text-slate-300 uppercase tracking-wider">
                <Sliders className="w-4 h-4 text-purple-400" />
                <span>{language === 'bn' ? 'কনভার্সন সেটিংস' : 'Conversion Options'}</span>
              </div>

              {/* Tool 1: PDF to PPTX options */}
              {tool.id === 'pdf-to-pptx' && (
                <div className="text-xs text-slate-300 space-y-1">
                  <p className="font-semibold text-white">
                    {language === 'bn' ? '১৬:৯ ওয়াইডস্ক্রিন পাওয়ারপয়েন্ট প্রেজেন্টেশন' : '16:9 Widescreen Presentation'}
                  </p>
                  <p className="text-slate-400">
                    {language === 'bn'
                      ? 'প্রতিটি পাতা স্পষ্ট ও আসল রেজোলিউশন বজায় রেখে স্লাইড আকারে রূপান্তর করা হবে।'
                      : 'Each page will be rendered into a high-DPI full-slide image in standard .pptx format.'}
                  </p>
                </div>
              )}

              {/* Tool 2: Merge PDF */}
              {tool.id === 'merge-pdf' && (
                <div className="text-xs text-slate-300">
                  <p className="text-slate-400">
                    {language === 'bn'
                      ? 'ডকুমেন্টগুলো যে ক্রমে রাখতে চান, তীর চিহ্ন দিয়ে সেভাবে সাজিয়ে নিন।'
                      : 'Rearrange files in your preferred sequence using the up/down arrows before merging.'}
                  </p>
                </div>
              )}

              {/* Tool 3: Image to PDF */}
              {tool.id === 'image-to-pdf' && (
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 text-xs">
                  <div>
                    <label className="block text-slate-400 mb-1">{t.pageSize}</label>
                    <select
                      value={imgToPdfOpts.pageSize}
                      onChange={(e) => setImgToPdfOpts({ ...imgToPdfOpts, pageSize: e.target.value as any })}
                      className="w-full bg-slate-900 border border-white/10 rounded-xl p-2 text-white"
                    >
                      <option value="A4">A4 (Standard)</option>
                      <option value="Letter">US Letter</option>
                      <option value="Fit">Fit to Image Size</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-slate-400 mb-1">{t.orientation}</label>
                    <select
                      value={imgToPdfOpts.orientation}
                      onChange={(e) => setImgToPdfOpts({ ...imgToPdfOpts, orientation: e.target.value as any })}
                      className="w-full bg-slate-900 border border-white/10 rounded-xl p-2 text-white"
                    >
                      <option value="auto">Auto (Smart Match)</option>
                      <option value="portrait">{t.portrait}</option>
                      <option value="landscape">{t.landscape}</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-slate-400 mb-1">{t.margins}</label>
                    <select
                      value={imgToPdfOpts.margin}
                      onChange={(e) => setImgToPdfOpts({ ...imgToPdfOpts, margin: Number(e.target.value) })}
                      className="w-full bg-slate-900 border border-white/10 rounded-xl p-2 text-white"
                    >
                      <option value="0">{t.none} (Full Bleed)</option>
                      <option value="20">{t.small} (20 pt)</option>
                      <option value="40">{t.normal} (40 pt)</option>
                    </select>
                  </div>
                </div>
              )}

              {/* Tool 4: PDF to Images */}
              {tool.id === 'pdf-to-images' && (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
                  <div>
                    <label className="block text-slate-400 mb-1">{t.format}</label>
                    <select
                      value={pdfToImgOpts.format}
                      onChange={(e) => setPdfToImgOpts({ ...pdfToImgOpts, format: e.target.value as any })}
                      className="w-full bg-slate-900 border border-white/10 rounded-xl p-2 text-white"
                    >
                      <option value="image/png">PNG (Lossless High Quality)</option>
                      <option value="image/jpeg">JPG (Compact File Size)</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-slate-400 mb-1">{t.scale}</label>
                    <select
                      value={pdfToImgOpts.scale}
                      onChange={(e) => setPdfToImgOpts({ ...pdfToImgOpts, scale: Number(e.target.value) })}
                      className="w-full bg-slate-900 border border-white/10 rounded-xl p-2 text-white"
                    >
                      <option value="1.0">1x (72 DPI - Standard Web)</option>
                      <option value="2.0">2x (150 DPI - Sharp Print Ready)</option>
                      <option value="3.0">3x (300 DPI - Ultra High Definition)</option>
                    </select>
                  </div>
                </div>
              )}

              {/* Tool 5: Image to Image */}
              {tool.id === 'image-to-image' && (
                <div className="space-y-3 text-xs">
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    <div>
                      <label className="block text-slate-400 mb-1">{t.format}</label>
                      <select
                        value={imgToImgOpts.targetFormat}
                        onChange={(e) => setImgToImgOpts({ ...imgToImgOpts, targetFormat: e.target.value as any })}
                        className="w-full bg-slate-900 border border-white/10 rounded-xl p-2 text-white"
                      >
                        <option value="image/webp">WebP (Modern, Smallest Size)</option>
                        <option value="image/jpeg">JPEG (Universal Compatibility)</option>
                        <option value="image/png">PNG (Lossless)</option>
                      </select>
                    </div>

                    <div>
                      <div className="flex justify-between text-slate-400 mb-1">
                        <span>{t.quality}</span>
                        <span className="text-white font-mono">{Math.round(imgToImgOpts.quality * 100)}%</span>
                      </div>
                      <input
                        type="range"
                        min="0.1"
                        max="1.0"
                        step="0.05"
                        value={imgToImgOpts.quality}
                        onChange={(e) => setImgToImgOpts({ ...imgToImgOpts, quality: Number(e.target.value) })}
                        className="w-full accent-purple-500"
                      />
                    </div>
                  </div>
                </div>
              )}

              {/* Tool 6: Text to PDF */}
              {tool.id === 'text-to-pdf' && (
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 text-xs">
                  <div>
                    <label className="block text-slate-400 mb-1">{t.fontSize}</label>
                    <input
                      type="number"
                      min="8"
                      max="28"
                      value={textToPdfOpts.fontSize}
                      onChange={(e) => setTextToPdfOpts({ ...textToPdfOpts, fontSize: Number(e.target.value) })}
                      className="w-full bg-slate-900 border border-white/10 rounded-xl p-2 text-white font-mono"
                    />
                  </div>

                  <div>
                    <label className="block text-slate-400 mb-1">{t.pageSize}</label>
                    <select
                      value={textToPdfOpts.pageSize}
                      onChange={(e) => setTextToPdfOpts({ ...textToPdfOpts, pageSize: e.target.value as any })}
                      className="w-full bg-slate-900 border border-white/10 rounded-xl p-2 text-white"
                    >
                      <option value="A4">A4</option>
                      <option value="Letter">US Letter</option>
                    </select>
                  </div>

                  <div className="flex items-center space-x-2 pt-5">
                    <input
                      type="checkbox"
                      id="pageNums"
                      checked={textToPdfOpts.addPageNumbers}
                      onChange={(e) => setTextToPdfOpts({ ...textToPdfOpts, addPageNumbers: e.target.checked })}
                      className="w-4 h-4 accent-purple-500 rounded"
                    />
                    <label htmlFor="pageNums" className="text-slate-300 font-medium cursor-pointer">
                      {t.pageNumbering}
                    </label>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Progress Indicator */}
          {status === 'converting' && (
            <div className="p-6 rounded-2xl bg-white/5 border border-white/10 text-center space-y-3">
              <RefreshCw className="w-8 h-8 text-purple-400 animate-spin mx-auto" />
              <p className="text-sm font-semibold text-white">{progressMsg}</p>
              <div className="w-full bg-black/40 rounded-full h-2 overflow-hidden border border-white/10">
                <div
                  className="h-full bg-gradient-to-r from-purple-500 to-pink-500 transition-all duration-300"
                  style={{ width: `${progressPercent}%` }}
                />
              </div>
            </div>
          )}

          {/* Success State */}
          {status === 'success' && (
            <div className="text-center py-6 space-y-6 animate-in zoom-in-95 duration-300">
              <div className="w-16 h-16 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 flex items-center justify-center mx-auto shadow-lg shadow-emerald-500/20">
                <CheckCircle2 className="w-9 h-9" />
              </div>

              <div>
                <h3 className="text-2xl font-bold text-white">
                  {t.successTitle}
                </h3>
                <p className="text-xs text-slate-400 mt-1">
                  {resultFileName} {resultBlob && `(${(resultBlob.size / 1024).toFixed(1)} KB)`}
                </p>
              </div>

              {/* Main Download Button */}
              {resultBlob && (
                <div className="flex flex-col sm:flex-row items-center justify-center gap-3">
                  <button
                    onClick={() => handleDownload(resultBlob, resultFileName)}
                    className="w-full sm:w-auto px-8 py-3.5 rounded-2xl font-bold text-sm text-white shadow-xl shadow-purple-500/25 flex items-center justify-center space-x-2 transition-all hover:scale-105 active:scale-95"
                    style={{
                      background: `linear-gradient(135deg, ${currentTheme.primaryAccent}, ${currentTheme.warmAccent})`,
                      color: currentTheme.isDark ? '#000000' : '#FFFFFF'
                    }}
                  >
                    <Download className="w-4 h-4" />
                    <span>{t.downloadBtn}</span>
                  </button>
                </div>
              )}

              {/* Extracted Images Grid for PDF to Images */}
              {extractedImages.length > 0 && (
                <div className="mt-6 pt-6 border-t border-white/10 text-left">
                  <p className="text-xs font-semibold text-slate-300 mb-3">
                    {language === 'bn' ? 'আলাদা আলাদা পাতা ডাউনলোড করুন:' : 'Download Individual Pages:'}
                  </p>
                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 max-h-48 overflow-y-auto pr-1">
                    {extractedImages.map((img) => (
                      <div
                        key={img.pageNumber}
                        onClick={() => handleDownload(img.blob, img.name)}
                        className="group relative rounded-xl border border-white/10 overflow-hidden cursor-pointer hover:border-purple-400 transition-all bg-black/40 p-1 text-center"
                      >
                        <img src={img.dataUrl} alt={img.name} className="w-full h-24 object-contain rounded-lg" />
                        <span className="text-[10px] text-slate-400 group-hover:text-purple-300 block mt-1">
                          Page {img.pageNumber}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <div className="pt-4">
                <button
                  onClick={() => {
                    setStatus('idle');
                    setFiles([]);
                    setResultBlob(null);
                    setExtractedImages([]);
                  }}
                  className="text-xs text-slate-400 hover:text-white underline"
                >
                  {t.convertAnother}
                </button>
              </div>
            </div>
          )}

          {/* Error Message */}
          {status === 'error' && errorMessage && (
            <div className="p-4 rounded-2xl bg-red-500/10 border border-red-500/30 flex items-start space-x-3 text-red-300 text-xs">
              <AlertCircle className="w-4 h-4 shrink-0 text-red-400 mt-0.5" />
              <span>{errorMessage}</span>
            </div>
          )}

        </div>

        {/* Modal Footer */}
        {status !== 'success' && (
          <div className="p-5 sm:p-6 border-t border-white/10 bg-white/5 flex items-center justify-between">
            <button
              onClick={onClose}
              className="px-5 py-2.5 rounded-xl text-xs font-semibold text-slate-400 hover:text-white transition-colors"
            >
              {t.close}
            </button>

            <button
              disabled={status === 'converting' || (tool.id !== 'text-to-pdf' && files.length === 0)}
              onClick={handleConvert}
              className="px-7 py-3 rounded-xl font-bold text-xs shadow-lg transition-all hover:scale-105 active:scale-95 disabled:opacity-40 disabled:cursor-not-allowed flex items-center space-x-2"
              style={{
                background: `linear-gradient(135deg, ${currentTheme.primaryAccent}, ${currentTheme.warmAccent})`,
                color: currentTheme.isDark ? '#000000' : '#FFFFFF'
              }}
            >
              <Sparkles className="w-4 h-4" />
              <span>{status === 'converting' ? t.processing : t.convertNow}</span>
            </button>
          </div>
        )}

      </div>
    </div>
  );
};
