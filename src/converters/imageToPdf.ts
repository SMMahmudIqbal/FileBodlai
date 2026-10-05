import { PDFDocument, PageSizes } from 'pdf-lib';

export interface ImageToPdfOptions {
  pageSize: 'A4' | 'Letter' | 'Fit';
  orientation: 'portrait' | 'landscape' | 'auto';
  margin: number; // in points (0, 20, 40)
}

// Convert any image file (JPG, PNG, WebP) to JPEG or PNG Uint8Array via Canvas
async function getCompatibleImageBytes(file: File): Promise<{ bytes: Uint8Array; isJpg: boolean; width: number; height: number }> {
  return new Promise((resolve, reject) => {
    const img = new Image();
    const objectUrl = URL.createObjectURL(file);

    img.onload = () => {
      URL.revokeObjectURL(objectUrl);
      const canvas = document.createElement('canvas');
      canvas.width = img.naturalWidth || img.width;
      canvas.height = img.naturalHeight || img.height;
      const ctx = canvas.getContext('2d');
      if (!ctx) {
        reject(new Error('Failed to get 2D context'));
        return;
      }

      // Draw white background
      ctx.fillStyle = '#FFFFFF';
      ctx.fillRect(0, 0, canvas.width, canvas.height);
      ctx.drawImage(img, 0, 0);

      canvas.toBlob(
        async (blob) => {
          if (!blob) {
            reject(new Error('Failed to convert canvas to blob'));
            return;
          }
          const buf = await blob.arrayBuffer();
          resolve({
            bytes: new Uint8Array(buf),
            isJpg: true,
            width: canvas.width,
            height: canvas.height,
          });
        },
        'image/jpeg',
        0.95
      );
    };

    img.onerror = () => {
      URL.revokeObjectURL(objectUrl);
      reject(new Error(`Failed to load image: ${file.name}`));
    };

    img.src = objectUrl;
  });
}

export async function convertImagesToPdf(
  files: File[],
  options: ImageToPdfOptions,
  onProgress?: (current: number, total: number) => void
): Promise<Blob> {
  if (files.length === 0) throw new Error('No images selected');

  const pdfDoc = await PDFDocument.create();

  for (let i = 0; i < files.length; i++) {
    if (onProgress) onProgress(i + 1, files.length);

    const { bytes, isJpg, width: imgW, height: imgH } = await getCompatibleImageBytes(files[i]);
    const embeddedImage = isJpg ? await pdfDoc.embedJpg(bytes) : await pdfDoc.embedPng(bytes);

    let pageWidth = PageSizes.A4[0];
    let pageHeight = PageSizes.A4[1];

    if (options.pageSize === 'Letter') {
      pageWidth = PageSizes.Letter[0];
      pageHeight = PageSizes.Letter[1];
    } else if (options.pageSize === 'Fit') {
      pageWidth = imgW + options.margin * 2;
      pageHeight = imgH + options.margin * 2;
    }

    if (options.pageSize !== 'Fit') {
      const isLandscape =
        options.orientation === 'landscape' ||
        (options.orientation === 'auto' && imgW > imgH);

      if (isLandscape && pageWidth < pageHeight) {
        const temp = pageWidth;
        pageWidth = pageHeight;
        pageHeight = temp;
      }
    }

    const page = pdfDoc.addPage([pageWidth, pageHeight]);
    const margin = options.margin;
    const availW = pageWidth - margin * 2;
    const availH = pageHeight - margin * 2;

    // Scale image while preserving aspect ratio
    const scale = Math.min(availW / imgW, availH / imgH);
    const renderW = imgW * scale;
    const renderH = imgH * scale;

    const x = margin + (availW - renderW) / 2;
    const y = margin + (availH - renderH) / 2;

    page.drawImage(embeddedImage, {
      x,
      y,
      width: renderW,
      height: renderH,
    });
  }

  const pdfBytes = await pdfDoc.save();
  return new Blob([pdfBytes as unknown as BlobPart], { type: 'application/pdf' });
}
