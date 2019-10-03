/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.dsl.symboltable;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.dsl.domain.DocumentSymbol;
import org.springframework.dsl.domain.SymbolInformation;
import org.springframework.dsl.symboltable.model.ClassSymbol;
import org.springframework.dsl.symboltable.model.FieldSymbol;
import org.springframework.dsl.symboltable.model.LocalScope;
import org.springframework.dsl.symboltable.model.PredefinedScope;
import org.springframework.dsl.symboltable.support.DocumentSymbolTableVisitor;

public class DocumentSymbolTableVisitorTests {

	@Test
	public void testSimpleVisit() {
		PredefinedScope scope = new PredefinedScope();

		ClassSymbol classA = new ClassSymbol("classA");
		scope.define(classA);

		ClassSymbol classB = new ClassSymbol("classB");
		scope.define(classB);

		FieldSymbol fieldA = new FieldSymbol("fieldA");
		classB.define(fieldA);

		DocumentSymbolTableVisitor visitor = new DocumentSymbolTableVisitor();
		scope.accept(visitor);
		assertThat(visitor.getSymbolizeInfo()).isNotNull();

		List<DocumentSymbol> documentSymbols = visitor.getSymbolizeInfo().documentSymbols().toStream()
				.collect(Collectors.toList());
		assertThat(documentSymbols).hasSize(2);
		assertThat(documentSymbols.get(0).getName()).isEqualTo("classA");
		assertThat(documentSymbols.get(1).getName()).isEqualTo("classB");
		assertThat(documentSymbols.get(1).getChildren()).hasSize(1);
		assertThat(documentSymbols.get(1).getChildren().get(0).getName()).isEqualTo("fieldA");

		List<SymbolInformation> symbolInformations = visitor.getSymbolizeInfo().symbolInformations().toStream()
				.collect(Collectors.toList());
		assertThat(symbolInformations).hasSize(3);
		assertThat(symbolInformations.get(0).getName()).isEqualTo("classA");
		assertThat(symbolInformations.get(1).getName()).isEqualTo("fieldA");
		assertThat(symbolInformations.get(2).getName()).isEqualTo("classB");
	}

	@Test
	public void testNestedScopes() {
		PredefinedScope scope = new PredefinedScope();

		LocalScope scopeA = new LocalScope(scope);
		scope.nest(scopeA);
		ClassSymbol classA = new ClassSymbol("classA");
		scopeA.define(classA);

		LocalScope scopeB = new LocalScope(scope);
		scope.nest(scopeB);
		ClassSymbol classB = new ClassSymbol("classA");
		scopeB.define(classB);

		DocumentSymbolTableVisitor visitor = new DocumentSymbolTableVisitor();
		scope.accept(visitor);
		assertThat(visitor.getSymbolizeInfo()).isNotNull();

		List<DocumentSymbol> documentSymbols = visitor.getSymbolizeInfo().documentSymbols().toStream()
				.collect(Collectors.toList());
		assertThat(documentSymbols).hasSize(2);
		assertThat(documentSymbols.get(0).getName()).isEqualTo("classA");
		assertThat(documentSymbols.get(0).getChildren()).isNull();
		assertThat(documentSymbols.get(1).getName()).isEqualTo("classA");
		assertThat(documentSymbols.get(1).getChildren()).isNull();
	}

	@Test
	public void testSimpleVisitQuery() {
		PredefinedScope scope = new PredefinedScope();

		ClassSymbol classA = new ClassSymbol("classA");
		scope.define(classA);

		ClassSymbol classB = new ClassSymbol("classB");
		scope.define(classB);

		FieldSymbol fieldA = new FieldSymbol("fieldA");
		classB.define(fieldA);

		DocumentSymbolTableVisitor visitor = new DocumentSymbolTableVisitor();
		visitor.setSymbolQuery(symbol -> {
			if (symbol instanceof FieldSymbol) {
				return true;
			}
			return false;
		});
		scope.accept(visitor);
		assertThat(visitor.getSymbolizeInfo()).isNotNull();

		List<SymbolInformation> symbolInformations = visitor.getSymbolizeInfo().symbolInformations().toStream()
				.collect(Collectors.toList());
		assertThat(symbolInformations).hasSize(1);
	}
}
