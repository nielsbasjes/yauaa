/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2022 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent.clienthints.parsers;

import lombok.Getter;
import nl.basjes.parse.useragent.clienthints.ClientHints.BrandVersion;
import nl.basjes.parse.useragent.parser.ClientHintsBaseVisitor;
import nl.basjes.parse.useragent.parser.ClientHintsLexer;
import nl.basjes.parse.useragent.parser.ClientHintsParser;
import nl.basjes.parse.useragent.utils.DefaultANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.List;

public class BrandVersionListParser extends ClientHintsBaseVisitor<Void> implements DefaultANTLRErrorListener {

    public static List<BrandVersion> parse(String inputString) {
        return new BrandVersionListParser(inputString).getResult();
    }

    @Getter
    private List<BrandVersion> result;

    public BrandVersionListParser(String inputString) {
        result = new ArrayList<>();
        CodePointCharStream input = CharStreams.fromString(inputString);
        ClientHintsLexer lexer = new ClientHintsLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(this);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ClientHintsParser parser = new ClientHintsParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(this);

        ParserRuleContext brandVersionListContext = parser.brandVersionList();
        visit(brandVersionListContext);
    }

    @Override
    public Void visitBrandVersion(ClientHintsParser.BrandVersionContext ctx) {
        if (ctx.brand == null || ctx.version == null) {
            return null;
        }
        String brandText = ctx.brand.getText();
        String versionText = ctx.version.getText();

        result.add(new BrandVersion(brandText, versionText));
        return null;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        // Ignore Syntax errors. People WILL try to hack this.
    }
}
