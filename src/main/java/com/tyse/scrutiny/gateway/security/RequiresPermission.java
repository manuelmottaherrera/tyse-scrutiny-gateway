package com.tyse.scrutiny.gateway.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Custom annotation to simplify permission checks in Spring Security.
 *
 * <p>This annotation is a shorthand for {@code @PreAuthorize("hasPermission(null, 'permission')")}
 * and provides a cleaner, more readable syntax for securing methods with granular permissions.
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * {@code
 * // Instead of:
 * @PreAuthorize("hasPermission(null, 'user.create')")
 * public Mono<User> createUser(User user) { ... }
 *
 * // You can use:
 * @RequiresPermission("user.create")
 * public Mono<User> createUser(User user) { ... }
 * }
 * </pre>
 *
 * <h3>Permission Format:</h3>
 * <p>Permissions should follow the "resource.action" pattern:
 * <ul>
 *   <li>{@code user.create} - Create users</li>
 *   <li>{@code user.read} - Read/view users</li>
 *   <li>{@code user.update} - Update users</li>
 *   <li>{@code user.delete} - Delete users</li>
 *   <li>{@code invoice.approve} - Approve invoices</li>
 *   <li>{@code report.export} - Export reports</li>
 * </ul>
 *
 * <h3>How It Works:</h3>
 * <p>When a method annotated with {@code @RequiresPermission} is invoked:
 * <ol>
 *   <li>Spring Security intercepts the method call</li>
 *   <li>The {@link EnterprisePermissionEvaluator} is invoked to check the permission</li>
 *   <li>The evaluator checks if the permission exists in the user's granted authorities</li>
 *   <li>If the user has the permission, the method executes; otherwise, an AccessDeniedException is thrown</li>
 * </ol>
 *
 * <h3>Multiple Permissions:</h3>
 * <p>For methods requiring multiple permissions, use standard {@code @PreAuthorize} with logical operators:
 * <pre>
 * {@code
 * @PreAuthorize("hasPermission(null, 'user.update') and hasPermission(null, 'user.sensitive.update')")
 * public Mono<User> updateSensitiveData(User user) { ... }
 * }
 * </pre>
 *
 * @see EnterprisePermissionEvaluator
 * @see org.springframework.security.access.prepost.PreAuthorize
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasPermission(null, #root.annotation.value)")
public @interface RequiresPermission {
    /**
     * The permission required to access the annotated method or class.
     *
     * <p>Format: "resource.action"
     * <p>Example: "user.create", "invoice.approve", "report.export"
     *
     * @return the permission string
     */
    String value();
}
