/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.mongodb.repository.query;

import lombok.RequiredArgsConstructor;
import reactor.util.context.Context;

import org.springframework.data.repository.query.ContextAwareQueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.spel.ContextAwareEvaluationContextProvider;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * Accessor to obtain plain {@link ExpressionEvaluatingParameterBinder} and contextualized
 * {@link ExpressionEvaluatingParameterBinder} if supported by {@link EvaluationContextProvider}.
 *
 * @author Mark Paluch
 * @since 2.1
 * @see Context
 */
abstract class ExpressionEvaluatingParameterBinderAccessor {

	/**
	 * Creates an {@link ExpressionEvaluatingParameterBinderAccessor}.
	 *
	 * @param expressionParser
	 * @param evaluationContextProvider
	 * @return
	 */
	static ExpressionEvaluatingParameterBinderAccessor of(SpelExpressionParser expressionParser,
			QueryMethodEvaluationContextProvider evaluationContextProvider) {

		if (evaluationContextProvider instanceof ContextAwareQueryMethodEvaluationContextProvider) {

			return new ContextualExpressionEvaluatingParameterBinderAccessor(expressionParser,
					(ContextAwareQueryMethodEvaluationContextProvider) evaluationContextProvider);
		}

		return new DefaultExpressionEvaluatingParameterBinderAccessor(expressionParser, evaluationContextProvider);

	}

	/**
	 * @return the plain {@link ExpressionEvaluatingParameterBinder}.
	 */
	abstract ExpressionEvaluatingParameterBinder getParameterBinder();

	/**
	 * @param context
	 * @return the contextualized {@link ExpressionEvaluatingParameterBinder} if contextualization is supported by
	 *         {@link EvaluationContextProvider}.
	 */
	abstract ExpressionEvaluatingParameterBinder getParameterBinder(Context context);

	/**
	 * Plain accessor.
	 */
	static class DefaultExpressionEvaluatingParameterBinderAccessor extends ExpressionEvaluatingParameterBinderAccessor {

		private final ExpressionEvaluatingParameterBinder parameterBinder;

		DefaultExpressionEvaluatingParameterBinderAccessor(SpelExpressionParser expressionParser,
				QueryMethodEvaluationContextProvider evaluationContextProvider) {

			this.parameterBinder = new ExpressionEvaluatingParameterBinder(expressionParser, evaluationContextProvider);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.mongodb.repository.query.ExpressionEvaluatingParameterBinderAccessor#getParameterBinder()
		 */
		@Override
		ExpressionEvaluatingParameterBinder getParameterBinder() {
			return parameterBinder;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.mongodb.repository.query.ExpressionEvaluatingParameterBinderAccessor#getParameterBinder(reactor.util.context.Context)
		 */
		@Override
		ExpressionEvaluatingParameterBinder getParameterBinder(Context context) {
			return getParameterBinder();
		}
	}

	/**
	 * Accessor applying {@link Context} to {@link ContextAwareEvaluationContextProvider}.
	 */
	@RequiredArgsConstructor
	static class ContextualExpressionEvaluatingParameterBinderAccessor
			extends ExpressionEvaluatingParameterBinderAccessor {

		private final SpelExpressionParser expressionParser;
		private final ContextAwareQueryMethodEvaluationContextProvider evaluationContextProvider;

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.mongodb.repository.query.ExpressionEvaluatingParameterBinderAccessor#getParameterBinder()
		 */
		@Override
		ExpressionEvaluatingParameterBinder getParameterBinder() {
			return new ExpressionEvaluatingParameterBinder(expressionParser, evaluationContextProvider);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.mongodb.repository.query.ExpressionEvaluatingParameterBinderAccessor#getParameterBinder(reactor.util.context.Context)
		 */
		@Override
		ExpressionEvaluatingParameterBinder getParameterBinder(Context context) {

			QueryMethodEvaluationContextProvider contextualized = evaluationContextProvider.withSubscriberContext(context);

			return new ExpressionEvaluatingParameterBinder(expressionParser, contextualized);
		}
	}
}
