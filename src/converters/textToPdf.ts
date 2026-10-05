import { PDFDocument, StandardFonts, rgb, PageSizes } from 'pdf-lib';

export interface TextToPdfOptions {
  fontSize: number;
  lineHeightMultiplier: number;
  pageSize: 'A4' | 'Letter';
  addPageNumbers: boolean;
  documentTitle?: string;
}

export async function convertTextToPdf(
  text: string,
  options: TextToPdfOptions
): Promise<Blob> {
  const pdfDoc = await PDFDocument.create();
  const font = await pdfDoc.embedFont(StandardFonts.Helvetica);
  const boldFont = await pdfDoc.embedFont(StandardFonts.HelveticaBold);

  const [pageW, pageH] = options.pageSize === 'Letter' ? PageSizes.Letter : PageSizes.A4;
  const margin = 48; // 48 pt margin (~0.67 in)
  const contentW = pageW - margin * 2;
  const contentH = pageH - margin * 2 - (options.documentTitle ? 30 : 0);

  const fontSize = options.fontSize;
  const lineHeight = fontSize * options.lineHeightMultiplier;

  // Split lines and handle word wrapping
  const rawParagraphs = text.split(/\r?\n/);
  const wrappedLines: string[] = [];

  for (const paragraph of rawParagraphs) {
    if (paragraph.trim() === '') {
      wrappedLines.push('');
      continue;
    }

    const words = paragraph.split(' ');
    let currentLine = '';

    for (const word of words) {
      const testLine = currentLine ? `${currentLine} ${word}` : word;
      const textWidth = font.widthOfTextAtSize(testLine, fontSize);

      if (textWidth <= contentW) {
        currentLine = testLine;
      } else {
        if (currentLine) wrappedLines.push(currentLine);
        currentLine = word;
      }
    }
    if (currentLine) wrappedLines.push(currentLine);
  }

  // Calculate pages
  const linesPerPage = Math.max(1, Math.floor(contentH / lineHeight));
  const totalPages = Math.ceil(wrappedLines.length / linesPerPage) || 1;

  for (let pageIdx = 0; pageIdx < totalPages; pageIdx++) {
    const page = pdfDoc.addPage([pageW, pageH]);
    let currentY = pageH - margin;

    // Optional Header
    if (options.documentTitle) {
      page.drawText(options.documentTitle, {
        x: margin,
        y: currentY,
        size: fontSize + 4,
        font: boldFont,
        color: rgb(0.1, 0.12, 0.18),
      });
      currentY -= 28;
    }

    // Render lines for this page
    const startLine = pageIdx * linesPerPage;
    const endLine = Math.min(startLine + linesPerPage, wrappedLines.length);

    for (let i = startLine; i < endLine; i++) {
      const line = wrappedLines[i];
      if (line) {
        page.drawText(line, {
          x: margin,
          y: currentY,
          size: fontSize,
          font: font,
          color: rgb(0.15, 0.18, 0.22),
        });
      }
      currentY -= lineHeight;
    }

    // Optional Page Number
    if (options.addPageNumbers) {
      const pageNumberText = `Page ${pageIdx + 1} of ${totalPages}`;
      const numWidth = font.widthOfTextAtSize(pageNumberText, 9);
      page.drawText(pageNumberText, {
        x: pageW - margin - numWidth,
        y: margin / 2,
        size: 9,
        font: font,
        color: rgb(0.5, 0.55, 0.6),
      });
    }
  }

  const pdfBytes = await pdfDoc.save();
  return new Blob([pdfBytes as unknown as BlobPart], { type: 'application/pdf' });
}
