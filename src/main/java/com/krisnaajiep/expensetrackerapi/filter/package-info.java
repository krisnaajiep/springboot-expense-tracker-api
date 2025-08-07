/**
 * This package contains filters that handle request/response processing and other
 * cross-cutting concerns in the HTTP request pipeline. Filter in this package
 * includes:
 * <ul>
 *     <li>{@link com.krisnaajiep.expensetrackerapi.filter.RateLimitFilter} to enforces rate limiting for incoming HTTP requests</li>
 *     <li>{@link com.krisnaajiep.expensetrackerapi.filter.ThrottlingFilter} to enforces throttling on incoming HTTP requests</li>
 * </ul>
 *
 * @since 0.0.1
 */
package com.krisnaajiep.expensetrackerapi.filter;