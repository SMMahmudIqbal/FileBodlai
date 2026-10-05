export interface ImageConvertOptions {
  targetFormat: 'image/jpeg' | 'image/png' | 'image/webp';
  quality: number; // 0.1 to 1.0
  resizeMode: 'original' | 'custom';
  customWidth?: number;
  customHeight?: number;
  maintainAspect?: boolean;
}

export interface ConvertedImageResult {
  blob: Blob;
  dataUrl: string;
  width: number;
  height: number;
  size: number;
  name: string;
}

export async function convertImage(
  file: File,
  options: ImageConvertOptions
): Promise<ConvertedImageResult> {
  return new Promise((resolve, reject) => {
    const img = new Image();
    const objectUrl = URL.createObjectURL(file);

    img.onload = () => {
      URL.revokeObjectURL(objectUrl);
      const originalW = img.naturalWidth || img.width;
      const originalH = img.naturalHeight || img.height;

      let targetW = originalW;
      let targetH = originalH;

      if (options.resizeMode === 'custom' && options.customWidth && options.customHeight) {
        targetW = Math.max(1, Math.round(options.customWidth));
        targetH = Math.max(1, Math.round(options.customHeight));
      }

      const canvas = document.createElement('canvas');
      canvas.width = targetW;
      canvas.height = targetH;

      const ctx = canvas.getContext('2d');
      if (!ctx) {
        reject(new Error('Canvas 2D context unavailable'));
        return;
      }

      // If converting to JPEG, fill white background to avoid transparent black artifacts
      if (options.targetFormat === 'image/jpeg') {
        ctx.fillStyle = '#FFFFFF';
        ctx.fillRect(0, 0, targetW, targetH);
      }

      ctx.imageSmoothingEnabled = true;
      ctx.imageSmoothingQuality = 'high';
      ctx.drawImage(img, 0, 0, targetW, targetH);

      canvas.toBlob(
        (blob) => {
          if (!blob) {
            reject(new Error('Image conversion failed'));
            return;
          }

          const extMap: Record<string, string> = {
            'image/jpeg': 'jpg',
            'image/png': 'png',
            'image/webp': 'webp',
          };
          const baseName = file.name.replace(/\.[^/.]+$/, '');
          const newExt = extMap[options.targetFormat] || 'jpg';
          const newName = `${baseName}_converted.${newExt}`;
          const dataUrl = canvas.toDataURL(options.targetFormat, options.quality);

          resolve({
            blob,
            dataUrl,
            width: targetW,
            height: targetH,
            size: blob.size,
            name: newName,
          });
        },
        options.targetFormat,
        options.quality
      );
    };

    img.onerror = () => {
      URL.revokeObjectURL(objectUrl);
      reject(new Error(`Failed to load image: ${file.name}`));
    };

    img.src = objectUrl;
  });
}
