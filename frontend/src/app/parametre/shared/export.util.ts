export interface ExportColumn {
  key: string;
  label: string;
  format?: (value: unknown, row: Record<string, unknown>) => string;
}

function cellValue(row: Record<string, unknown>, col: ExportColumn): string {
  const raw = col.format ? col.format(row[col.key], row) : row[col.key];
  if (raw === null || raw === undefined) {
    return '';
  }
  return String(raw);
}

function downloadBlob(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
}

export function exportToExcel(
  rows: Record<string, unknown>[],
  columns: ExportColumn[],
  filename: string
): void {
  const header = columns.map(c => `<th>${c.label}</th>`).join('');
  const body = rows.map(row =>
    `<tr>${columns.map(c => `<td>${cellValue(row, c)}</td>`).join('')}</tr>`
  ).join('');

  const html = `
    <html xmlns:o="urn:schemas-microsoft-com:office:office"
          xmlns:x="urn:schemas-microsoft-com:office:excel">
      <head><meta charset="UTF-8"></head>
      <body>
        <table border="1">
          <thead><tr>${header}</tr></thead>
          <tbody>${body}</tbody>
        </table>
      </body>
    </html>`;

  const blob = new Blob(['\ufeff', html], {
    type: 'application/vnd.ms-excel;charset=utf-8;'
  });
  downloadBlob(blob, `${filename}.xls`);
}

export function exportToPdf(
  rows: Record<string, unknown>[],
  columns: ExportColumn[],
  title: string,
  filename: string
): void {
  const header = columns.map(c => `<th>${c.label}</th>`).join('');
  const body = rows.map(row =>
    `<tr>${columns.map(c => `<td>${cellValue(row, c)}</td>`).join('')}</tr>`
  ).join('');

  const win = window.open('', '_blank');
  if (!win) {
    return;
  }

  win.document.write(`
    <!DOCTYPE html>
    <html lang="fr">
      <head>
        <meta charset="UTF-8">
        <title>${title}</title>
        <style>
          body { font-family: 'Segoe UI', sans-serif; padding: 24px; color: #002B5B; }
          h1 { font-size: 18px; margin-bottom: 16px; }
          table { width: 100%; border-collapse: collapse; font-size: 12px; }
          th { background: #002B5B; color: white; padding: 8px; text-align: left; }
          td { padding: 8px; border-bottom: 1px solid #e5e7eb; }
          tr:nth-child(even) { background: #f9fafb; }
          @media print { body { padding: 0; } }
        </style>
      </head>
      <body>
        <h1>${title}</h1>
        <table>
          <thead><tr>${header}</tr></thead>
          <tbody>${body}</tbody>
        </table>
      </body>
    </html>`);
  win.document.close();
  win.focus();
  setTimeout(() => {
    win.print();
    win.close();
  }, 300);
}
