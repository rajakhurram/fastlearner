import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'stripHtml',
})
export class StripHtmlPipe implements PipeTransform {
  transform(value?: string | null): string {
    if (!value) {
      return '';
    }

    let text = value
      // Block-level tags (open + close) ko space se replace karo taake words aapas me chipke na
      .replace(/<\/?(p|div|li|ul|ol|h[1-6]|br|tr|td)[^>]*>/gi, ' ')
      // Baaki saare remaining tags (span, b, i, strong, etc.) hatao — content preserve rahega
      .replace(/<[^>]*>/g, '');

    // Numeric entities (&#160;, &#8211;, &#10;, &#183; etc.)
    text = text.replace(/&#(\d+);/g, (_m, dec) =>
      String.fromCharCode(parseInt(dec, 10)),
    );

    // Hex numeric entities (&#x...;)
    text = text.replace(/&#x([0-9a-f]+);/gi, (_m, hex) =>
      String.fromCharCode(parseInt(hex, 16)),
    );

    // Named entities
    text = text
      .replace(/&nbsp;/gi, ' ')
      .replace(/&amp;/gi, '&')
      .replace(/&lt;/gi, '<')
      .replace(/&gt;/gi, '>')
      .replace(/&quot;/gi, '"')
      .replace(/&#39;/gi, "'");

    // Extra whitespace normalize + trim
    return text.replace(/\s+/g, ' ').trim();
  }
}
