package local.booking.security;

import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.web.header.HeaderWriterFilter;

/** Official CVE-2026-22732 workaround; not a replacement for upgrading Security. */
final class EagerSecurityHeaders {
    private EagerSecurityHeaders() { }
    static ObjectPostProcessor<HeaderWriterFilter> processor() {
        return new ObjectPostProcessor<HeaderWriterFilter>() {
            @Override public <O extends HeaderWriterFilter> O postProcess(O filter) {
                filter.setShouldWriteHeadersEagerly(true);
                return filter;
            }
        };
    }
}
