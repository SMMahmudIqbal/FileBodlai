import JSZip from 'jszip';
import { loadPdfDocument, renderPageToCanvas } from '../utils/pdfHelper';

export interface ExtractedImage {
  pageNumber: number;
  blob: Blob;
  dataUrl: string;
  name: string;
}

export async function convertPdfToImages(
  file: File,
  options: {
    format: 'image/png' | 'image/jpeg';
    scale: number;
  },
  onProgress?: (current: number, total: number) => void
): Promise<ExtractedImage[]> {
  const pdfDoc = await loadPdfDocument(file);
  const totalPages = pdfDoc.numPages;
  const results: ExtractedImage[] = [];

  const baseName = file.name.replace(/\.[^/.]+$/, '');
  const extension = options.format === 'image/jpeg' ? 'jpg' : 'png';

  for (let i = 1; i <= totalPages; i++) {
    if (onProgress) onProgress(i, totalPages);

    const { canvas } = await renderPageToCanvas(pdfDoc, i, options.scale);

    const dataUrl = canvas.toDataURL(options.format, 0.95);
    const blob: Blob = await new Promise((resolve, reject) => {
      canvas.toBlob(
        (b) => {
          if (b) resolve(b);
          else reject(new Error('Canvas to blob conversion failed'));
        },
        options.format,
        0.95
      );
    });

    results.push({
      pageNumber: i,
      blob,
      dataUrl,
      name: `${baseName}_page_${String(i).padStart(3, '0')}.${extension}`,
    });
  }

  return results;
}

export async function createZipFromImages(images: ExtractedImage[], _zipName = 'pages.zip'): Promise<Blob> {
  const zip = new JSZip();
  images.forEach((img) => {
    zip.file(img.name, img.blob);
  });
  return await zip.generateAsync({ type: 'blob' });
}
