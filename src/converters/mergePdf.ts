import { PDFDocument } from 'pdf-lib';

export async function mergePdfFiles(
  files: File[],
  onProgress?: (current: number, total: number) => void
): Promise<Blob> {
  if (files.length === 0) throw new Error('No PDF files provided to merge');

  const mergedPdf = await PDFDocument.create();

  for (let i = 0; i < files.length; i++) {
    if (onProgress) onProgress(i + 1, files.length);
    const arrayBuffer = await files[i].arrayBuffer();
    
    // Ignore encryption errors gracefully or load
    const srcDoc = await PDFDocument.load(arrayBuffer, { ignoreEncryption: true });
    const pageIndices = srcDoc.getPageIndices();
    const copiedPages = await mergedPdf.copyPages(srcDoc, pageIndices);
    
    for (const page of copiedPages) {
      mergedPdf.addPage(page);
    }
  }

  const mergedPdfBytes = await mergedPdf.save();
  return new Blob([mergedPdfBytes as unknown as BlobPart], { type: 'application/pdf' });
}
