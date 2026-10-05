import * as pdfjsLib from 'pdfjs-dist';

// Set worker source to our public worker file
if (typeof window !== 'undefined') {
  pdfjsLib.GlobalWorkerOptions.workerSrc = '/pdf.worker.min.js';
}

export async function loadPdfDocument(file: File) {
  const arrayBuffer = await file.arrayBuffer();
  const loadingTask = pdfjsLib.getDocument({ data: new Uint8Array(arrayBuffer) });
  return await loadingTask.promise;
}

export async function renderPageToCanvas(
  pdfDoc: pdfjsLib.PDFDocumentProxy,
  pageNumber: number,
  scale = 2.0
): Promise<{ canvas: HTMLCanvasElement; width: number; height: number }> {
  const page = await pdfDoc.getPage(pageNumber);
  const viewport = page.getViewport({ scale });

  const canvas = document.createElement('canvas');
  const context = canvas.getContext('2d');
  if (!context) throw new Error('Could not get 2D canvas context');

  canvas.width = viewport.width;
  canvas.height = viewport.height;

  // Fill canvas with white background (prevents transparent PDF page rendering issues)
  context.fillStyle = '#FFFFFF';
  context.fillRect(0, 0, canvas.width, canvas.height);

  const renderContext = {
    canvasContext: context,
    viewport: viewport,
  };

  await page.render(renderContext).promise;
  return { canvas, width: viewport.width, height: viewport.height };
}
