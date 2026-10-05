import pptxgen from 'pptxgenjs';
import { loadPdfDocument, renderPageToCanvas } from '../utils/pdfHelper';

export async function convertPdfToPptx(
  file: File,
  onProgress?: (current: number, total: number) => void
): Promise<Blob> {
  const pptx = new pptxgen();
  // Set layout to 16:9 widescreen presentation
  pptx.layout = 'LAYOUT_16x9';

  const pdfDoc = await loadPdfDocument(file);
  const totalPages = pdfDoc.numPages;

  for (let pageNum = 1; pageNum <= totalPages; pageNum++) {
    if (onProgress) onProgress(pageNum, totalPages);

    const { canvas, width, height } = await renderPageToCanvas(pdfDoc, pageNum, 2.0);
    const imgData = canvas.toDataURL('image/jpeg', 0.92);

    const slide = pptx.addSlide();
    
    // Calculate aspect-preserving centered image or full bleed
    const isWidescreen = width / height >= 1.5;
    if (isWidescreen) {
      slide.addImage({
        data: imgData,
        x: 0,
        y: 0,
        w: '100%',
        h: '100%'
      });
    } else {
      // For standard portrait pages (e.g. A4/Letter), center on 16:9 slide with subtle dark backdrop
      slide.background = { color: '0D132D' };
      const slideHeight = 5.625; // 16:9 default height in inches
      const slideWidth = 10.0;
      const imgAspect = width / height;
      const renderH = slideHeight;
      const renderW = slideHeight * imgAspect;
      const renderX = (slideWidth - renderW) / 2;

      slide.addImage({
        data: imgData,
        x: renderX,
        y: 0,
        w: renderW,
        h: renderH
      });
    }
  }

  const output = await pptx.write({ outputType: 'blob' });
  return output as Blob;
}
