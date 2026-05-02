package com.github.xpenatan.jParser.ffm;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.utils.Pair;
import com.github.xpenatan.jParser.core.JParser;
import com.github.xpenatan.jParser.core.JParserHelper;
import com.github.xpenatan.jParser.core.JParserItem;
import com.github.xpenatan.jParser.idl.IDLAttribute;
import com.github.xpenatan.jParser.idl.IDLClass;
import com.github.xpenatan.jParser.idl.IDLConstructor;
import com.github.xpenatan.jParser.idl.IDLEnumClass;
import com.github.xpenatan.jParser.idl.IDLEnumItem;
import com.github.xpenatan.jParser.idl.IDLFile;
import com.github.xpenatan.jParser.idl.IDLHelper;
import com.github.xpenatan.jParser.idl.IDLMethod;
import com.github.xpenatan.jParser.idl.IDLParameter;
import com.github.xpenatan.jParser.idl.IDLReader;
import com.github.xpenatan.jParser.idl.parser.IDLAttributeOperation;
import com.github.xpenatan.jParser.idl.parser.IDLDefaultCodeParser;
import com.github.xpenatan.jParser.idl.parser.IDLMethodOperation;
import com.github.xpenatan.jParser.idl.parser.IDLMethodParser;
import com.github.xpenatan.jParser.idl.parser.data.IDLParameterData;
import java.util.ArrayList;
import java.util.List;

/**
 * FFM code parser that generates Java classes using java.lang.foreign MethodHandle downcalls
 * instead of JNI native methods. Parallel to CppCodeParser.
 *
 * <p>For each native method, generates:
 * <ul>
 *   <li>A private static (non-native) bridge method that invokes a MethodHandle</li>
 *   <li>C++ glue code using extern "C" with standard C types (via FFMCppGenerator)</li>
 * </ul>
 */
public class FFMCodeParser extends IDLDefaultCodeParser {

    private static final String HEADER_CMD = "FFM";
    private static final String CALLBACK_UPCALL_ARENA_FIELD = "upcallArena";
    private static final String CALLBACK_RELEASE_METHOD = "releaseUpcallResources";

    // Same template tags as CppCodeParser (the C++ code is largely the same)
    protected static final String TEMPLATE_TAG_TYPE = "[TYPE]";
    protected static final String TEMPLATE_TAG_METHOD = "[METHOD]";
    protected static final String TEMPLATE_TAG_OPERATOR = "[OPERATOR]";
    protected static final String TEMPLATE_TAG_ATTRIBUTE = "[ATTRIBUTE]";
    protected static final String TEMPLATE_TAG_ENUM = "[ENUM]";
    protected static final String TEMPLATE_TAG_ATTRIBUTE_TYPE = "[ATTRIBUTE_TYPE]";
    protected static final String TEMPLATE_TAG_RETURN_TYPE = "[RETURN_TYPE]";
    protected static final String TEMPLATE_TAG_CONST = "[CONST]";
    protected static final String TEMPLATE_TAG_COPY_TYPE = "[COPY_TYPE]";
    protected static final String TEMPLATE_TAG_COPY_PARAM = "[COPY_PARAM]";
    protected static final String TEMPLATE_TAG_CONSTRUCTOR = "[CONSTRUCTOR]";
    protected static final String TEMPLATE_TAG_CAST = "[CAST]";

    // C++ templates — identical to CppCodeParser but with int64_t casts instead of jlong
    protected static final String GET_CONSTRUCTOR_OBJ_POINTER_TEMPLATE =
            "\nreturn (int64_t)new [CONSTRUCTOR];\n";

    protected static final String METHOD_DELETE_OBJ_POINTER_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "delete nativeObject;\n";

    // --- Attribute templates (int64_t instead of jlong, int32_t instead of jint) ---

    protected static final String ATTRIBUTE_SET_PRIMITIVE_STATIC_TEMPLATE =
            "\n[TYPE]::[ATTRIBUTE] = [ATTRIBUTE];\n";

    protected static final String ATTRIBUTE_ARRAY_SET_PRIMITIVE_STATIC_TEMPLATE =
            "\n[TYPE]::[ATTRIBUTE][index] = [ATTRIBUTE];\n";

    protected static final String ATTRIBUTE_SET_PRIMITIVE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "nativeObject->[ATTRIBUTE] = [CAST][ATTRIBUTE];\n";

    protected static final String ATTRIBUTE_ARRAY_SET_PRIMITIVE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "nativeObject->[ATTRIBUTE][index] = [CAST][ATTRIBUTE];\n";

    protected static final String ATTRIBUTE_SET_OBJECT_POINTER_STATIC_TEMPLATE =
            "\n[TYPE]::[ATTRIBUTE] = ([ATTRIBUTE_TYPE]*)[ATTRIBUTE]_addr;\n";

    protected static final String ATTRIBUTE_ARRAY_SET_OBJECT_POINTER_STATIC_TEMPLATE =
            "\n[TYPE]::[ATTRIBUTE][index] = ([ATTRIBUTE_TYPE]*)[ATTRIBUTE]_addr;\n";

    protected static final String ATTRIBUTE_SET_OBJECT_POINTER_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "nativeObject->[ATTRIBUTE] = ([ATTRIBUTE_TYPE]*)[ATTRIBUTE]_addr;\n";

    protected static final String ATTRIBUTE_ARRAY_SET_OBJECT_POINTER_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "nativeObject->[ATTRIBUTE][index] = ([ATTRIBUTE_TYPE]*)[ATTRIBUTE]_addr;\n";

    protected static final String ATTRIBUTE_SET_OBJECT_VALUE_STATIC_TEMPLATE =
            "\n[TYPE]::[ATTRIBUTE] = *(([ATTRIBUTE_TYPE]*)[ATTRIBUTE]_addr);\n";

    protected static final String ATTRIBUTE_ARRAY_SET_OBJECT_VALUE_STATIC_TEMPLATE =
            "\n[TYPE]::[ATTRIBUTE][index] = *(([ATTRIBUTE_TYPE]*)[ATTRIBUTE]_addr);\n";

    protected static final String ATTRIBUTE_SET_OBJECT_VALUE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "nativeObject->[ATTRIBUTE] = *(([ATTRIBUTE_TYPE]*)[ATTRIBUTE]_addr);\n";

    protected static final String ATTRIBUTE_ARRAY_SET_OBJECT_VALUE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "nativeObject->[ATTRIBUTE][index] = *(([ATTRIBUTE_TYPE]*)[ATTRIBUTE]_addr);\n";

    protected static final String ATTRIBUTE_GET_OBJECT_VALUE_STATIC_TEMPLATE =
            "\nreturn (int64_t)&[TYPE]::[ATTRIBUTE];\n";

    protected static final String ATTRIBUTE_ARRAY_GET_OBJECT_VALUE_STATIC_TEMPLATE =
            "\nreturn (int64_t)&[TYPE]::[ATTRIBUTE][index];\n";

    protected static final String ATTRIBUTE_GET_OBJECT_VALUE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return (int64_t)&nativeObject->[ATTRIBUTE];\n";

    protected static final String ATTRIBUTE_ARRAY_GET_OBJECT_VALUE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return (int64_t)&nativeObject->[ATTRIBUTE][index];\n";

    protected static final String ATTRIBUTE_GET_OBJECT_POINTER_STATIC_TEMPLATE =
            "\nreturn (int64_t)[TYPE]::[ATTRIBUTE];\n";

    protected static final String ATTRIBUTE_ARRAY_GET_OBJECT_POINTER_STATIC_TEMPLATE =
            "\nreturn (int64_t)([TYPE]::[ATTRIBUTE][index]);\n";

    protected static final String ATTRIBUTE_GET_OBJECT_POINTER_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "[CONST][ATTRIBUTE_TYPE]* attr = nativeObject->[ATTRIBUTE];\n" +
            "return (int64_t)attr;\n";

    protected static final String ATTRIBUTE_ARRAY_GET_OBJECT_POINTER_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "[CONST][ATTRIBUTE_TYPE]* attr = (nativeObject->[ATTRIBUTE][index]);\n" +
            "return (int64_t)attr;\n";

    protected static final String ATTRIBUTE_GET_PRIMITIVE_STATIC_TEMPLATE =
            "\nreturn [TYPE]::[ATTRIBUTE];\n";

    protected static final String ATTRIBUTE_ARRAY_GET_PRIMITIVE_STATIC_TEMPLATE =
            "\nreturn [TYPE]::[ATTRIBUTE][index];\n";

    protected static final String ATTRIBUTE_GET_PRIMITIVE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return [CAST]nativeObject->[ATTRIBUTE];\n";

    protected static final String ATTRIBUTE_ARRAY_GET_PRIMITIVE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return [CAST]nativeObject->[ATTRIBUTE][index];\n";

    // --- Method templates ---

    protected static final String METHOD_GET_OBJ_VALUE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "static [COPY_TYPE] [COPY_PARAM];\n" +
            "[COPY_PARAM] = nativeObject->[METHOD];\n" +
            "return (int64_t)&[COPY_PARAM];";

    protected static final String METHOD_GET_OBJ_VALUE_ARITHMETIC_OPERATOR_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "static [COPY_TYPE] [COPY_PARAM];\n" +
            "[COPY_PARAM] = [OPERATOR];\n" +
            "return (int64_t)&[COPY_PARAM];";

    protected static final String METHOD_GET_OBJ_VALUE_STATIC_TEMPLATE =
            "\nstatic [COPY_TYPE] [COPY_PARAM];\n" +
            "[COPY_PARAM] = [TYPE]::[METHOD];\n" +
            "return (int64_t)&[COPY_PARAM];";

    protected static final String METHOD_CALL_VOID_STATIC_TEMPLATE =
            "\n[TYPE]::[METHOD];\n";

    protected static final String METHOD_CALL_VOID_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "nativeObject->[METHOD];\n";

    protected static final String METHOD_GET_OBJ_POINTER_STATIC_TEMPLATE =
            "\nreturn (int64_t)[TYPE]::[METHOD];\n";

    protected static final String METHOD_GET_OBJ_POINTER_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "[CONST][RETURN_TYPE]* obj = nativeObject->[METHOD];\n" +
            "return (int64_t)obj;\n";

    protected static final String METHOD_GET_OBJ_POINTER_OPERATOR_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "[CONST][RETURN_TYPE]* obj = [OPERATOR];\n" +
            "return (int64_t)obj;\n";

    protected static final String METHOD_GET_REF_OBJ_POINTER_STATIC_TEMPLATE =
            "\nreturn (int64_t)&[TYPE]::[METHOD];\n";

    protected static final String METHOD_GET_REF_OBJ_POINTER_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return (int64_t)&nativeObject->[METHOD];\n";

    protected static final String METHOD_GET_REF_OBJ_POINTER_OPERATOR_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return (int64_t)&[OPERATOR];\n";

    protected static final String METHOD_GET_PRIMITIVE_STATIC_TEMPLATE =
            "\nreturn [CAST][TYPE]::[METHOD];\n";

    protected static final String METHOD_GET_PRIMITIVE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return [CAST]nativeObject->[METHOD];\n";

    protected static final String METHOD_GET_PRIMITIVE_OPERATOR_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return ([OPERATOR]);";

    protected static final String ENUM_GET_INT_TEMPLATE =
            "\nreturn (int64_t)[ENUM];\n";

    private final FFMNativeCodeGenerator cppGenerator;
    private final FFMMethodHandleRegistry registry = new FFMMethodHandleRegistry();

    public FFMCodeParser(FFMNativeCodeGenerator cppGenerator, String cppDir) {
        this(cppGenerator, null, "", cppDir);
    }

    public FFMCodeParser(FFMNativeCodeGenerator cppGenerator, IDLReader idlReader, String basePackage, String cppDir) {
        super(basePackage, HEADER_CMD, idlReader, cppDir);
        this.cppGenerator = cppGenerator;
    }

    // ==================== IDL Generation Hooks ====================

    @Override
    public void onIDLConstructorGenerated(JParser jParser, IDLConstructor idlConstructor,
                                          ClassOrInterfaceDeclaration classDeclaration,
                                          ConstructorDeclaration constructorDeclaration,
                                          MethodDeclaration nativeMethodDeclaration) {
        IDLClass idlClass = idlConstructor.idlClass;
        String classTypeName = idlClass.getCPPName();

        NodeList<Parameter> parameters = constructorDeclaration.getParameters();
        ArrayList<IDLParameter> idParameters = idlConstructor.parameters;
        String params = getParams(parameters, idParameters);

        String constructor = classTypeName + "(" + params + ")";
        String content = GET_CONSTRUCTOR_OBJ_POINTER_TEMPLATE.replace(TEMPLATE_TAG_CONSTRUCTOR, constructor);

        String header = "[-" + HEADER_CMD + ";" + CMD_NATIVE + "]";
        String blockComment = header + content;
        nativeMethodDeclaration.setBlockComment(blockComment);
    }

    @Override
    public void onIDLDeConstructorGenerated(JParser jParser, IDLClass idlClass,
                                            ClassOrInterfaceDeclaration classDeclaration,
                                            MethodDeclaration nativeMethodDeclaration) {
        String classTypeName;
        if(idlClass.callbackImpl == null) {
            classTypeName = idlClass.getCPPName();
        }
        else {
            classTypeName = idlClass.callbackImpl.name;
        }

        String content = METHOD_DELETE_OBJ_POINTER_TEMPLATE.replace(TEMPLATE_TAG_TYPE, classTypeName);

        String header = "[-" + HEADER_CMD + ";" + CMD_NATIVE + "]";
        String blockComment = header + content;
        nativeMethodDeclaration.setBlockComment(blockComment);
    }

    @Override
    public void onIDLMethodGenerated(JParser jParser, IDLMethod idlMethod,
                                     ClassOrInterfaceDeclaration classDeclaration,
                                     MethodDeclaration methodDeclaration,
                                     MethodDeclaration nativeMethodDeclaration) {
        String param = getParams(idlMethod, methodDeclaration);
        setupMethodGenerated(idlMethod, param, classDeclaration, methodDeclaration, nativeMethodDeclaration);
    }

    @Override
    public void onIDLAttributeGenerated(JParser jParser, IDLAttribute idlAttribute, boolean isSet,
                                        ClassOrInterfaceDeclaration classDeclaration,
                                        MethodDeclaration methodDeclaration,
                                        MethodDeclaration nativeMethod) {
        String attributeName = idlAttribute.name;
        String classTypeName = classDeclaration.getNameAsString();
        IDLClass idlClass = idlAttribute.idlFile.getClass(classTypeName);
        if(idlClass != null) {
            classTypeName = idlClass.getCPPName();
        }

        String getPrimitiveCast = "";
        String attributeType = idlAttribute.getCPPType();
        String constTag = "";
        if(idlAttribute.isConst) {
            constTag = "const ";
        }

        IDLClass retTypeClass = idlAttribute.idlFile.getClass(attributeType);
        if(retTypeClass != null) {
            attributeType = retTypeClass.getCPPName();
        }

        if(idlAttribute.isAny) {
            getPrimitiveCast = "(int64_t)";
        }

        String attributeReturnCast = "";

        IDLEnumClass idlEnum = idlAttribute.idlFile.getEnum(attributeType);
        if(idlEnum != null) {
            if(idlEnum.typePrefix.equals(attributeType)) {
                attributeReturnCast = "(" + attributeType + ")";
            }
            else {
                attributeReturnCast = "(" + idlEnum.typePrefix + "::" + attributeType + ")";
            }
            getPrimitiveCast = "(int32_t)";
        }

        String content = null;
        IDLAttributeOperation.Op op = IDLAttributeOperation.getEnum(isSet, idlAttribute, methodDeclaration, nativeMethod);
        switch(op) {
            case SET_OBJECT_VALUE:
                content = ATTRIBUTE_SET_OBJECT_VALUE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_ATTRIBUTE_TYPE, attributeType).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case SET_ARRAY_OBJECT_VALUE:
                content = ATTRIBUTE_ARRAY_SET_OBJECT_VALUE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_ATTRIBUTE_TYPE, attributeType).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case SET_OBJECT_VALUE_STATIC:
                content = ATTRIBUTE_SET_OBJECT_VALUE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_ATTRIBUTE_TYPE, attributeType).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case SET_ARRAY_OBJECT_VALUE_STATIC:
                content = ATTRIBUTE_ARRAY_SET_OBJECT_VALUE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_ATTRIBUTE_TYPE, attributeType).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_OBJECT_VALUE:
                content = ATTRIBUTE_GET_OBJECT_VALUE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_ATTRIBUTE_TYPE, attributeType).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_ARRAY_OBJECT_VALUE:
                content = ATTRIBUTE_ARRAY_GET_OBJECT_VALUE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_ATTRIBUTE_TYPE, attributeType).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_OBJECT_VALUE_STATIC:
                content = ATTRIBUTE_GET_OBJECT_VALUE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_ATTRIBUTE_TYPE, attributeType).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_ARRAY_OBJECT_VALUE_STATIC:
                content = ATTRIBUTE_ARRAY_GET_OBJECT_VALUE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_ATTRIBUTE_TYPE, attributeType).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case SET_OBJECT_POINTER:
                content = ATTRIBUTE_SET_OBJECT_POINTER_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_ATTRIBUTE_TYPE, attributeType).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case SET_ARRAY_OBJECT_POINTER:
                content = ATTRIBUTE_ARRAY_SET_OBJECT_POINTER_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_ATTRIBUTE_TYPE, attributeType).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case SET_OBJECT_POINTER_STATIC:
                content = ATTRIBUTE_SET_OBJECT_POINTER_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_ATTRIBUTE_TYPE, attributeType).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case SET_ARRAY_OBJECT_POINTER_STATIC:
                content = ATTRIBUTE_ARRAY_SET_OBJECT_POINTER_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_ATTRIBUTE_TYPE, attributeType).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_OBJECT_POINTER:
                content = ATTRIBUTE_GET_OBJECT_POINTER_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName).replace(TEMPLATE_TAG_ATTRIBUTE_TYPE, attributeType).replace(TEMPLATE_TAG_CONST, constTag);
                break;
            case GET_ARRAY_OBJECT_POINTER:
                content = ATTRIBUTE_ARRAY_GET_OBJECT_POINTER_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName).replace(TEMPLATE_TAG_ATTRIBUTE_TYPE, attributeType).replace(TEMPLATE_TAG_CONST, constTag);
                break;
            case GET_OBJECT_POINTER_STATIC:
                content = ATTRIBUTE_GET_OBJECT_POINTER_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_ARRAY_OBJECT_POINTER_STATIC:
                content = ATTRIBUTE_ARRAY_GET_OBJECT_POINTER_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case SET_PRIMITIVE:
                content = ATTRIBUTE_SET_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName).replace(TEMPLATE_TAG_CAST, attributeReturnCast);
                break;
            case SET_PRIMITIVE_STATIC:
                content = ATTRIBUTE_SET_PRIMITIVE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case SET_ARRAY_PRIMITIVE_STATIC:
                content = ATTRIBUTE_ARRAY_SET_PRIMITIVE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case SET_ARRAY_PRIMITIVE:
                content = ATTRIBUTE_ARRAY_SET_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName).replace(TEMPLATE_TAG_CAST, attributeReturnCast);
                break;
            case GET_PRIMITIVE:
                content = ATTRIBUTE_GET_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName).replace(TEMPLATE_TAG_CAST, getPrimitiveCast);
                break;
            case GET_ARRAY_PRIMITIVE:
                content = ATTRIBUTE_ARRAY_GET_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName).replace(TEMPLATE_TAG_CAST, getPrimitiveCast);
                break;
            case GET_PRIMITIVE_STATIC:
                content = ATTRIBUTE_GET_PRIMITIVE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_ARRAY_PRIMITIVE_STATIC:
                content = ATTRIBUTE_ARRAY_GET_PRIMITIVE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
        }

        if(content != null) {
            String header = "[-" + HEADER_CMD + ";" + CMD_NATIVE + "]";
            String blockComment = header + content;
            nativeMethod.setBlockComment(blockComment);
        }
    }

    @Override
    public void onIDLEnumMethodGenerated(JParser jParser, IDLEnumClass idlEnum,
                                         EnumDeclaration enumDeclaration,
                                         IDLEnumItem enumItem,
                                         MethodDeclaration nativeMethodDeclaration) {
        String enumStr = enumItem.name;
        String content = ENUM_GET_INT_TEMPLATE.replace(TEMPLATE_TAG_ENUM, enumStr);
        String header = "[-" + HEADER_CMD + ";" + CMD_NATIVE + "]";
        String blockComment = header + content;
        nativeMethodDeclaration.setBlockComment(blockComment);
    }

    @Override
    public void onIDLCallbackGenerated(JParser jParser, IDLClass idlClass,
                                       ClassOrInterfaceDeclaration classDeclaration,
                                       MethodDeclaration callbackDeclaration,
                                       ArrayList<Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>>> methods) {
        IDLClass idlCallbackClass = idlClass.callbackImpl;

        ensureCallbackUpcallMembers(classDeclaration, methods);
        ensureDeleteNativeReleasesUpcalls(classDeclaration);

        // 1. Build parameter list for native setupCallback: this_addr + one long per callback method (function pointer)
        ArrayList<IDLParameterData> parameterArray = new ArrayList<>();
        for(Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> pair : methods) {
            IDLMethod idlMethod = pair.a;
            String fpParamName = idlMethod.getCPPName() + "_fp";
            Parameter fpParam = new Parameter(com.github.javaparser.ast.type.PrimitiveType.longType(), fpParamName);
            IDLParameterData data = new IDLParameterData();
            data.parameter = fpParam;
            parameterArray.add(data);
        }

        Type methodReturnType = callbackDeclaration.getType();
        MethodDeclaration nativeMethodDeclaration = IDLMethodParser.generateNativeMethod(
                idlReader, callbackDeclaration.getNameAsString(), parameterArray, methodReturnType, false);

        if(!JParserHelper.containsMethod(classDeclaration, nativeMethodDeclaration)) {
            // Keep the method static (FFM uses explicit this_addr, no implicit JNI params)
            classDeclaration.getMembers().add(nativeMethodDeclaration);

            // 2. Build setupCallback Java body with upcall stub creation
            StringBuilder body = new StringBuilder();
            body.append("{\n");
            body.append("    try {\n");
            body.append("        ").append(CALLBACK_RELEASE_METHOD).append("();\n");
            body.append("        ").append(CALLBACK_UPCALL_ARENA_FIELD).append(" = java.lang.foreign.Arena.ofShared();\n");

            for(Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> pair : methods) {
                IDLMethod idlMethod = pair.a;
                MethodDeclaration internalMethod = pair.b.a;
                String methodName = idlMethod.getCPPName();
                String internalMethodName = internalMethod.getNameAsString();
                String stubFieldName = getCallbackStubFieldName(methodName);

                // FFM upcall stubs require MethodHandle types to exactly match the FunctionDescriptor.
                // For String (const char*) parameters, the native side passes a pointer (ADDRESS layout),
                // so the internal method must accept MemorySegment instead of String and convert it.
                fixupCallbackStringParams(internalMethod);

                String methodTypeStr = buildMethodTypeStr(internalMethod);
                String funcDescriptor = buildCallbackFunctionDescriptor(internalMethod);

                body.append("        java.lang.invoke.MethodHandle mh_").append(methodName)
                    .append(" = java.lang.invoke.MethodHandles.lookup().findVirtual(")
                    .append(classDeclaration.getNameAsString()).append(".class, \"")
                    .append(internalMethodName).append("\", ").append(methodTypeStr).append(").bindTo(this);\n");
                body.append("        ").append(stubFieldName)
                    .append(" = java.lang.foreign.Linker.nativeLinker().upcallStub(mh_").append(methodName)
                    .append(", ").append(funcDescriptor).append(", ").append(CALLBACK_UPCALL_ARENA_FIELD).append(");\n");
            }

            // Call native setupCallback with native_address + stub addresses
            body.append("        ").append(nativeMethodDeclaration.getNameAsString()).append("(native_address");
            for(Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> pair : methods) {
                IDLMethod idlMethod = pair.a;
                body.append(", ").append(getCallbackStubFieldName(idlMethod.getCPPName())).append(".address()");
            }
            body.append(");\n");

            body.append("    } catch(Throwable e) {\n");
            body.append("        throw new RuntimeException(e);\n");
            body.append("    }\n");
            body.append("}");

            BlockStmt blockStmt = StaticJavaParser.parseBlock(body.toString());
            callbackDeclaration.setBody(blockStmt);

            // 3. Set C++ code for the native setupCallback method
            String cppSetupBody = generateFFMSetupCallbackCPPBody(idlCallbackClass, methods);
            String header = "[-" + HEADER_CMD + ";" + CMD_NATIVE + "]";
            nativeMethodDeclaration.setBlockComment(header + cppSetupBody);

            // 4. Generate C++ callback class and emit it via the generator
            generateFFMCPPClass(idlClass, classDeclaration, callbackDeclaration, methods);
        }
    }

    // ==================== Code Block Parsing ====================

    @Override
    public boolean parseCodeBlock(Node node, String headerCommands, String content) {
        if(!super.parseCodeBlock(node, headerCommands, content)) {
            if(headerCommands.contains(CMD_NATIVE)) {
                cppGenerator.addNativeCode(node, content);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void setJavaBodyNativeCMD(String content, MethodDeclaration methodDeclaration) {
        // Collect C++ code for the FFM glue file
        cppGenerator.addNativeCode(methodDeclaration, content);

        // Register the MethodHandle entry for this native method
        String handleName = registerNativeMethod(methodDeclaration);

        // Transform the native method into an FFM bridge method
        convertToFFMBridgeMethod(methodDeclaration, handleName);
    }

    // ==================== Lifecycle Hooks ====================

    @Override
    public void onParseClassStart(JParser jParser, CompilationUnit unit, TypeDeclaration classOrEnum) {
        String nameAsString = classOrEnum.getNameAsString();
        String include = classCppPath.get(nameAsString);
        super.onParseClassStart(jParser, unit, classOrEnum);
    }

    @Override
    public void onParseFileEnd(JParser jParser, JParserItem parserItem) {
        cppGenerator.addParseFile(jParser, parserItem);
    }

    @Override
    public void onParseEnd(JParser jParser) {
        cppGenerator.generate(jParser);
    }

    @Override
    public void onParserComplete(JParser jParser, ArrayList<JParserItem> parserItems) {
        super.onParserComplete(jParser, parserItems);

        // For each class that has registered MethodHandle entries, inject the FFMHandles inner class
        for(JParserItem parserItem : parserItems) {
            if(parserItem.notAllowed) continue;

            ClassOrInterfaceDeclaration classDeclaration = parserItem.getClassDeclaration();
            if(classDeclaration != null) {
                String className = classDeclaration.getNameAsString();
                if(registry.hasEntries(className)) {
                    injectFFMHandlesClass(parserItem.unit, classDeclaration, className);
                }
                continue;
            }

            // Also handle enum declarations (they can have native methods too)
            EnumDeclaration enumDeclaration = parserItem.getEnumDeclaration();
            if(enumDeclaration != null) {
                String className = enumDeclaration.getNameAsString();
                if(registry.hasEntries(className)) {
                    injectFFMHandlesClassForEnum(parserItem.unit, enumDeclaration, className);
                }
            }
        }
    }

    // ==================== FFM Bridge Method Generation ====================

    /**
     * Register a native method in the MethodHandle registry.
     * Returns the unique handle name (method name + overload suffix) for use in bridge method body.
     */
    private String registerNativeMethod(MethodDeclaration methodDeclaration) {
        TypeDeclaration classOrEnum = (TypeDeclaration) methodDeclaration.getParentNode().get();
        CompilationUnit compilationUnit = classOrEnum.findCompilationUnit().get();
        String packageName = compilationUnit.getPackageDeclaration().get().getNameAsString();
        String className = classOrEnum.getNameAsString();
        String methodName = methodDeclaration.getNameAsString();

        // Build parameter info
        List<FFMMethodHandleRegistry.ParamInfo> paramInfos = new ArrayList<>();
        ArrayList<FFMCppGenerator.FFMArgument> ffmArgs = new ArrayList<>();
        if(methodDeclaration.getParameters() != null) {
            for(Parameter parameter : methodDeclaration.getParameters()) {
                FFMMethodHandleRegistry.ParamInfo paramInfo = FFMMethodHandleRegistry.ParamInfo.fromParameter(parameter);
                paramInfos.add(paramInfo);

                String[] typeTokens = parameter.getType().toString().split("\\.");
                String type = typeTokens[typeTokens.length - 1];
                ffmArgs.add(new FFMCppGenerator.FFMArgument(
                        parameter.getNameAsString(), type,
                        FFMTypeMapper.getCType(type),
                        FFMTypeMapper.getOverloadSuffix(type)));
            }
        }

        // Build overload suffix for unique handle name
        StringBuilder overloadSuffix = new StringBuilder();
        for(FFMCppGenerator.FFMArgument arg : ffmArgs) {
            overloadSuffix.append(arg.overloadSuffix);
        }
        String handleName = methodName + "__" + overloadSuffix;

        String returnType = methodDeclaration.getType().toString();
        String symbolName = FFMCppGenerator.buildSymbolName(packageName, className, methodName, ffmArgs);

        registry.register(className, symbolName, methodName, handleName, returnType, paramInfos);
        return handleName;
    }

    /**
     * Transform a JNI-style native method declaration into an FFM bridge method.
     * Removes the 'native' modifier and adds a body that invokes the MethodHandle.
     *
     * @param handleName the unique field name in FFMHandles (includes overload suffix)
     */
    private void convertToFFMBridgeMethod(MethodDeclaration methodDeclaration, String handleName) {
        // Remove native modifier
        methodDeclaration.removeModifier(Modifier.Keyword.NATIVE);

        String methodName = methodDeclaration.getNameAsString();
        Type returnType = methodDeclaration.getType();
        String returnTypeStr = returnType.asString();
        boolean isVoid = returnType.isVoidType();

        // Build the invokeExact call arguments
        StringBuilder invokeArgs = new StringBuilder();
        NodeList<Parameter> parameters = methodDeclaration.getParameters();
        for(int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            if(i > 0) invokeArgs.append(", ");

            String paramType = parameter.getType().asString();
            // For String parameters, we need to convert to MemorySegment
            if(paramType.equals("String")) {
                invokeArgs.append("(java.lang.foreign.MemorySegment)(").append(parameter.getNameAsString())
                          .append(" != null ? java.lang.foreign.Arena.global().allocateFrom(")
                          .append(parameter.getNameAsString()).append(") : java.lang.foreign.MemorySegment.NULL)");
            }
            else {
                invokeArgs.append(parameter.getNameAsString());
            }
        }

        // Build method body
        StringBuilder bodyCode = new StringBuilder();
        bodyCode.append("{\n");
        bodyCode.append("    try {\n");

        if(isVoid) {
            bodyCode.append("        FFMHandles.").append(handleName)
                    .append(".invokeExact(").append(invokeArgs).append(");\n");
        }
        else if(FFMTypeMapper.isString(returnTypeStr)) {
            // String returns: native function returns const char* (ADDRESS).
            // invokeExact returns MemorySegment — convert to Java String.
            bodyCode.append("        java.lang.foreign.MemorySegment _retSeg = (java.lang.foreign.MemorySegment) FFMHandles.").append(handleName)
                    .append(".invokeExact(").append(invokeArgs).append(");\n");
            bodyCode.append("        return _retSeg.reinterpret(Long.MAX_VALUE).getString(0);\n");
        }
        else {
            String castType = FFMTypeMapper.getFFMCast(returnTypeStr);
            bodyCode.append("        return (").append(castType).append(") FFMHandles.").append(handleName)
                    .append(".invokeExact(").append(invokeArgs).append(");\n");
        }

        bodyCode.append("    } catch(Throwable e) {\n");
        bodyCode.append("        throw new RuntimeException(e);\n");
        bodyCode.append("    }\n");

        if(!isVoid) {
            // Unreachable but makes the compiler happy
        }

        bodyCode.append("}");

        BlockStmt body = StaticJavaParser.parseBlock(bodyCode.toString());
        methodDeclaration.setBody(body);
    }

    /**
     * Inject the FFMHandles inner class into a Java class with all MethodHandle field declarations.
     */
    private void injectFFMHandlesClass(CompilationUnit unit, ClassOrInterfaceDeclaration classDeclaration, String className) {
        String innerClassSource = buildFFMHandlesSource(className);
        if(innerClassSource == null) return;

        ClassOrInterfaceDeclaration innerClass = StaticJavaParser.parseBodyDeclaration(innerClassSource)
                .asClassOrInterfaceDeclaration();
        classDeclaration.addMember(innerClass);

        addFFMImports(unit);
    }

    /**
     * Inject the FFMHandles inner class into an enum declaration.
     */
    private void injectFFMHandlesClassForEnum(CompilationUnit unit, EnumDeclaration enumDeclaration, String className) {
        String innerClassSource = buildFFMHandlesSource(className);
        if(innerClassSource == null) return;

        ClassOrInterfaceDeclaration innerClass = StaticJavaParser.parseBodyDeclaration(innerClassSource)
                .asClassOrInterfaceDeclaration();
        enumDeclaration.addMember(innerClass);

        addFFMImports(unit);
    }

    /**
     * Build the FFMHandles inner class source code for a given class name.
     */
    private String buildFFMHandlesSource(String className) {
        List<FFMMethodHandleRegistry.FFMEntry> entries = registry.getEntries(className);
        if(entries.isEmpty()) return null;

        StringBuilder sb = new StringBuilder();
        sb.append("private static final class FFMHandles {\n");
        sb.append("    private static final java.lang.foreign.SymbolLookup LOOKUP;\n");
        sb.append("    private static final java.lang.foreign.Linker LINKER = java.lang.foreign.Linker.nativeLinker();\n");
        sb.append("    static {\n");
        sb.append("        LOOKUP = java.lang.foreign.SymbolLookup.loaderLookup();\n");
        sb.append("    }\n\n");

        for(FFMMethodHandleRegistry.FFMEntry entry : entries) {
            String descriptor = FFMMethodHandleRegistry.buildFunctionDescriptor(entry);
            sb.append("    static final java.lang.invoke.MethodHandle ").append(entry.handleName)
              .append(" = LINKER.downcallHandle(\n");
            sb.append("        LOOKUP.find(\"").append(entry.symbolName).append("\").orElseThrow(),\n");
            sb.append("        ").append(descriptor).append(");\n\n");
        }

        sb.append("}");
        return sb.toString();
    }

    private void addFFMImports(CompilationUnit unit) {
        unit.addImport("java.lang.foreign.FunctionDescriptor");
        unit.addImport("java.lang.foreign.ValueLayout");
        unit.addImport("java.lang.foreign.Linker");
        unit.addImport("java.lang.foreign.SymbolLookup");
        unit.addImport("java.lang.foreign.Arena");
        unit.addImport("java.lang.foreign.MemorySegment");
        unit.addImport("java.lang.invoke.MethodHandle");
    }

    // ==================== FFM Callback C++ Generation ====================

    /**
     * Generate the full C++ callback class with function pointers and emit it.
     * Attaches the class definition as a block comment on the constructor (same pattern as CppCodeParser).
     */
    private void generateFFMCPPClass(IDLClass idlClass, ClassOrInterfaceDeclaration classDeclaration,
                                     MethodDeclaration callbackDeclaration,
                                     ArrayList<Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>>> methods) {
        IDLClass callback = idlClass.callbackImpl;
        StringBuilder cppClass = new StringBuilder();

        // Generate function pointer typedefs
        for(Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> pair : methods) {
            IDLMethod idlMethod = pair.a;
            MethodDeclaration internalMethod = pair.b.a;
            cppClass.append(buildFPTypedef(callback.name, idlMethod, internalMethod)).append("\n");
        }
        cppClass.append("\n");

        // Class definition
        cppClass.append("class ").append(callback.getCPPName()).append(" : public ").append(idlClass.getCPPName()).append(" {\n");
        cppClass.append("private:\n");

        // Function pointer fields
        for(Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> pair : methods) {
            IDLMethod idlMethod = pair.a;
            MethodDeclaration internalMethod = pair.b.a;
            String fpTypeName = buildFPTypeName(callback.name, idlMethod, internalMethod);
            cppClass.append("\t").append(fpTypeName).append(" ").append(idlMethod.getCPPName()).append("_ptr;\n");
        }

        cppClass.append("public:\n");

        // setupCallback method — receives function pointers
        cppClass.append("\tvoid ").append(callbackDeclaration.getNameAsString()).append("(");
        for(int i = 0; i < methods.size(); i++) {
            Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> pair = methods.get(i);
            IDLMethod idlMethod = pair.a;
            MethodDeclaration internalMethod = pair.b.a;
            String fpTypeName = buildFPTypeName(callback.name, idlMethod, internalMethod);
            if(i > 0) cppClass.append(", ");
            cppClass.append(fpTypeName).append(" ").append(idlMethod.getCPPName());
        }
        cppClass.append(") {\n");
        for(Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> pair : methods) {
            IDLMethod idlMethod = pair.a;
            cppClass.append("\t\tthis->").append(idlMethod.getCPPName()).append("_ptr = ").append(idlMethod.getCPPName()).append(";\n");
        }
        cppClass.append("\t}\n");

        // Virtual methods — call function pointers
        cppClass.append(generateFFMMethodCallers(idlClass, methods));

        cppClass.append("};\n");

        // Attach to constructor block comment (same pattern as CppCodeParser).
        // parseCodeBlock will emit the code via cppGenerator.addNativeCode().
        String header = "[-" + HEADER_CMD + ";" + CMD_NATIVE + "]\n";
        String code = header + cppClass.toString();
        classDeclaration.getConstructors().get(0).setBlockComment(code);
    }

    /**
     * Generate the C++ body for the native setupCallback method.
     * Example: nativeObject->setupCallback((fp_type)fp1, (fp_type)fp2);
     */
    private String generateFFMSetupCallbackCPPBody(IDLClass idlCallbackClass,
                                                   ArrayList<Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>>> methods) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(idlCallbackClass.name).append("* nativeObject = (").append(idlCallbackClass.name).append("*)this_addr;\n");
        sb.append("nativeObject->setupCallback(");
        for(int i = 0; i < methods.size(); i++) {
            Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> pair = methods.get(i);
            IDLMethod idlMethod = pair.a;
            MethodDeclaration internalMethod = pair.b.a;
            String fpTypeName = buildFPTypeName(idlCallbackClass.name, idlMethod, internalMethod);
            if(i > 0) sb.append(", ");
            sb.append("(").append(fpTypeName).append(")").append(idlMethod.getCPPName()).append("_fp");
        }
        sb.append(");\n");
        return sb.toString();
    }

    /**
     * Generate virtual method implementations that call function pointers.
     */
    private String generateFFMMethodCallers(IDLClass idlClass,
                                            ArrayList<Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>>> methods) {
        IDLClass callback = idlClass.callbackImpl;
        StringBuilder cppMethods = new StringBuilder();

        for(Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> pair : methods) {
            IDLMethod idlMethod = pair.a;
            MethodDeclaration publicMethod = pair.b.b;

            Type type = publicMethod.getType();
            boolean isVoidType = type.isVoidType();
            String returnTypeStr = getFFMCPPType(idlMethod.getCPPReturnType());
            String constStr = idlMethod.isReturnConst ? " const" : "";
            String methodName = idlMethod.getCPPName();

            // Build virtual method params and call params
            StringBuilder methodParams = new StringBuilder();
            StringBuilder callParams = new StringBuilder();
            NodeList<Parameter> publicMethodParameters = publicMethod.getParameters();

            for(int i = 0; i < idlMethod.parameters.size(); i++) {
                IDLParameter idlParameter = idlMethod.parameters.get(i);
                Parameter parameter = publicMethodParameters.get(i);
                boolean isPrimitive = parameter.getType().isPrimitiveType() || idlParameter.isAny;
                String paramName = idlParameter.name;
                String paramType = idlParameter.getCPPType();
                boolean isString = idlParameter.idlType.equals("DOMString");
                String tag = " ";
                String callParamCast = "";

                if(!isString) {
                    if(idlParameter.isRef) {
                        tag = "& ";
                        callParamCast = "(int64_t)&";
                    }
                    else if(idlParameter.isAny) {
                        // any type = void* in C++ virtual method; needs (int64_t) cast for function pointer
                        // Don't change tag — getCPPType() already returns "void*"
                        callParamCast = "(int64_t)";
                    }
                    else if(!idlParameter.isEnum() && !isPrimitive && !idlParameter.isValue) {
                        tag = "* ";
                        callParamCast = "(int64_t)";
                    }
                }

                paramType = getFFMCPPType(paramType);
                if(idlParameter.isConst) {
                    paramType = "const " + paramType;
                }

                String callParamExpr = callParamCast + paramName;
                if(idlParameter.isEnum()) {
                    callParamExpr = "static_cast<int32_t>(" + paramName + ")";
                }

                if(i > 0) {
                    callParams.append(", ");
                    methodParams.append(", ");
                }
                callParams.append(callParamExpr);
                methodParams.append(paramType).append(tag).append(paramName);
            }

            String returnStr = isVoidType ? "" : "return (" + returnTypeStr + ")";
            if(returnTypeStr.contains("unsigned")) {
                returnStr = "return (" + returnTypeStr + ")";
            }

            cppMethods.append("\tvirtual ").append(returnTypeStr).append(" ").append(methodName)
                       .append("(").append(methodParams).append(")").append(constStr).append(" {\n");
            cppMethods.append("\t\t").append(returnStr).append(methodName).append("_ptr(").append(callParams).append(");\n");
            cppMethods.append("\t}\n");
        }
        return cppMethods.toString();
    }

    /**
     * Build a function pointer typedef for a callback method.
     * Example: typedef void (*fp_MyCallbackImpl_onEvent_JJ)(int64_t, int64_t);
     */
    private String buildFPTypedef(String className, IDLMethod idlMethod, MethodDeclaration internalMethod) {
        String fpTypeName = buildFPTypeName(className, idlMethod, internalMethod);
        String returnCType = FFMTypeMapper.getCType(internalMethod.getType().asString());

        StringBuilder sb = new StringBuilder();
        sb.append("typedef ").append(returnCType).append(" (*").append(fpTypeName).append(")(");
        NodeList<Parameter> params = internalMethod.getParameters();
        for(int i = 0; i < params.size(); i++) {
            if(i > 0) sb.append(", ");
            // Use IDL parameter info to detect string (DOMString) types, since
            // fixupCallbackStringParams may have changed the Java type to MemorySegment.
            if(i < idlMethod.parameters.size() && idlMethod.parameters.get(i).idlType.equals("DOMString")) {
                sb.append("const char*");
            } else {
                String paramType = params.get(i).getType().asString();
                sb.append(FFMTypeMapper.getCType(paramType));
            }
        }
        sb.append(");");
        return sb.toString();
    }

    /**
     * Build a unique function pointer type name for a callback method.
     * Example: fp_MyCallbackImpl_onEvent_JJ
     */
    private String buildFPTypeName(String className, IDLMethod idlMethod, MethodDeclaration internalMethod) {
        StringBuilder suffix = new StringBuilder();
        NodeList<Parameter> params = internalMethod.getParameters();
        for(Parameter param : params) {
            suffix.append(FFMTypeMapper.getOverloadSuffix(param.getType().asString()));
        }
        return "fp_" + className + "_" + idlMethod.getCPPName() + "_" + suffix;
    }

    /**
     * Fix up String parameters on a callback internal method for FFM upcall compatibility.
     * Changes the parameter type from String to MemorySegment and inserts conversion code
     * at the start of the method body (MemorySegment → String via getString(0)).
     */
    private void fixupCallbackStringParams(MethodDeclaration internalMethod) {
        NodeList<Parameter> params = internalMethod.getParameters();
        for(int i = 0; i < params.size(); i++) {
            Parameter param = params.get(i);
            if(param.getType().asString().equals("String")) {
                String originalName = param.getNameAsString();
                String segmentName = originalName + "_seg";
                param.setName(segmentName);
                param.setType(StaticJavaParser.parseType("java.lang.foreign.MemorySegment"));
                // Insert conversion statement at the top of the method body
                String convStmt = "String " + originalName + " = " + segmentName
                        + ".reinterpret(Long.MAX_VALUE).getString(0);";
                internalMethod.getBody().ifPresent(body ->
                        body.getStatements().add(0, StaticJavaParser.parseStatement(convStmt)));
            }
        }
    }

    /**
     * Build MethodType string for MethodHandles.lookup().findVirtual().
     * Example: java.lang.invoke.MethodType.methodType(void.class, long.class, long.class)
     */
    private String buildMethodTypeStr(MethodDeclaration internalMethod) {
        StringBuilder sb = new StringBuilder();
        sb.append("java.lang.invoke.MethodType.methodType(");
        Type returnType = internalMethod.getType();
        if(returnType.isVoidType()) {
            sb.append("void.class");
        } else {
            sb.append(returnType.asString()).append(".class");
        }
        for(Parameter param : internalMethod.getParameters()) {
            sb.append(", ").append(param.getType().asString()).append(".class");
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Build FunctionDescriptor for upcall stubs.
     * Example: java.lang.foreign.FunctionDescriptor.ofVoid(java.lang.foreign.ValueLayout.JAVA_LONG)
     */
    private String buildCallbackFunctionDescriptor(MethodDeclaration internalMethod) {
        StringBuilder sb = new StringBuilder();
        Type returnType = internalMethod.getType();
        boolean isVoid = returnType.isVoidType();

        if(isVoid) {
            sb.append("java.lang.foreign.FunctionDescriptor.ofVoid(");
        } else {
            String retLayout = FFMTypeMapper.getValueLayout(returnType.asString());
            if(retLayout == null) retLayout = "java.lang.foreign.ValueLayout.JAVA_LONG";
            else retLayout = "java.lang.foreign." + retLayout;
            sb.append("java.lang.foreign.FunctionDescriptor.of(").append(retLayout);
            if(internalMethod.getParameters().size() > 0) sb.append(", ");
        }

        NodeList<Parameter> params = internalMethod.getParameters();
        for(int i = 0; i < params.size(); i++) {
            if(i > 0) sb.append(", ");
            String paramType = params.get(i).getType().asString();
            String layout = FFMTypeMapper.getValueLayout(paramType);
            if(layout == null) layout = "java.lang.foreign.ValueLayout.JAVA_LONG";
            else layout = "java.lang.foreign." + layout;
            sb.append(layout);
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Map Java/IDL type to FFM-compatible C++ type.
     */
    private String getFFMCPPType(String typeString) {
        if(typeString.equals("boolean")) return "bool";
        if(typeString.equals("String")) return "char*";
        return typeString;
    }

    // ==================== C++ Parameter Helpers (reused from CppCodeParser) ====================

    private void setupMethodGenerated(IDLMethod idlMethod, String param,
                                      ClassOrInterfaceDeclaration classDeclaration,
                                      MethodDeclaration methodDeclaration,
                                      MethodDeclaration nativeMethod) {
        Type returnType = methodDeclaration.getType();
        String returnTypeStr = idlMethod.getJavaReturnType();
        String cppReturnType = idlMethod.getCPPReturnType();
        String methodName = idlMethod.getCPPName();
        String classTypeName = classDeclaration.getNameAsString();
        IDLClass idlClass = idlMethod.idlFile.getClass(classTypeName);
        if(idlClass != null) {
            classTypeName = idlClass.getCPPName();
        }
        String returnCastStr = "";
        String methodCaller = methodName + "(" + param + ")";
        if(idlMethod.idlFile.getEnum(returnTypeStr) != null) {
            returnCastStr = "(int)";
        }
        if(idlMethod.isAny) {
            returnCastStr = "(int64_t)";
        }

        String constTag = "";
        if(idlMethod.isReturnConst) {
            constTag = "const ";
        }

        String operator = getOperator(idlMethod.operator, param);
        String content = null;
        IDLMethodOperation.Op op = IDLMethodOperation.getEnum(idlMethod, methodDeclaration, nativeMethod);
        switch(op) {
            case CALL_VOID_STATIC:
                content = METHOD_CALL_VOID_STATIC_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case CALL_VOID:
                content = METHOD_CALL_VOID_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_OBJ_REF_POINTER_STATIC:
                content = METHOD_GET_REF_OBJ_POINTER_STATIC_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_OBJ_REF_POINTER:
                if(operator.isEmpty()) {
                    content = METHOD_GET_REF_OBJ_POINTER_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
                } else {
                    content = METHOD_GET_REF_OBJ_POINTER_OPERATOR_TEMPLATE.replace(TEMPLATE_TAG_OPERATOR, operator).replace(TEMPLATE_TAG_TYPE, classTypeName);
                }
                break;
            case GET_OBJ_VALUE_STATIC: {
                String returnTypeName = returnType.asClassOrInterfaceType().asClassOrInterfaceType().getNameAsString();
                IDLClass retTypeClass = idlMethod.idlFile.getClass(returnTypeName);
                if(retTypeClass != null) returnTypeName = retTypeClass.getCPPName();
                String copyParam = "copy_addr";
                content = METHOD_GET_OBJ_VALUE_STATIC_TEMPLATE
                        .replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName)
                        .replace(TEMPLATE_TAG_COPY_TYPE, returnTypeName).replace(TEMPLATE_TAG_COPY_PARAM, copyParam);
                break;
            }
            case GET_OBJ_VALUE: {
                String returnTypeName = returnType.asClassOrInterfaceType().asClassOrInterfaceType().getNameAsString();
                IDLClass retTypeClass = idlMethod.idlFile.getClass(returnTypeName);
                if(retTypeClass != null) returnTypeName = retTypeClass.getCPPName();
                String copyParam = "copy_addr";
                if(operator.isEmpty()) {
                    content = METHOD_GET_OBJ_VALUE_TEMPLATE
                            .replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName)
                            .replace(TEMPLATE_TAG_COPY_TYPE, returnTypeName).replace(TEMPLATE_TAG_COPY_PARAM, copyParam);
                } else {
                    content = METHOD_GET_OBJ_VALUE_ARITHMETIC_OPERATOR_TEMPLATE
                            .replace(TEMPLATE_TAG_OPERATOR, operator).replace(TEMPLATE_TAG_TYPE, classTypeName)
                            .replace(TEMPLATE_TAG_COPY_TYPE, returnTypeName).replace(TEMPLATE_TAG_COPY_PARAM, copyParam);
                }
                break;
            }
            case GET_OBJ_POINTER_STATIC:
                content = METHOD_GET_OBJ_POINTER_STATIC_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_OBJ_POINTER:
                if(operator.isEmpty()) {
                    content = METHOD_GET_OBJ_POINTER_TEMPLATE
                            .replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName)
                            .replace(TEMPLATE_TAG_RETURN_TYPE, cppReturnType).replace(TEMPLATE_TAG_CONST, constTag);
                } else {
                    content = METHOD_GET_OBJ_POINTER_OPERATOR_TEMPLATE
                            .replace(TEMPLATE_TAG_OPERATOR, operator).replace(TEMPLATE_TAG_TYPE, classTypeName)
                            .replace(TEMPLATE_TAG_RETURN_TYPE, cppReturnType).replace(TEMPLATE_TAG_CONST, constTag);
                }
                break;
            case GET_PRIMITIVE_STATIC:
                content = METHOD_GET_PRIMITIVE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName).replace(TEMPLATE_TAG_CAST, returnCastStr);
                break;
            case GET_PRIMITIVE:
                if(operator.isEmpty()) {
                    content = METHOD_GET_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName).replace(TEMPLATE_TAG_CAST, returnCastStr);
                } else {
                    content = METHOD_GET_PRIMITIVE_OPERATOR_TEMPLATE
                            .replace(TEMPLATE_TAG_OPERATOR, operator).replace(TEMPLATE_TAG_TYPE, classTypeName);
                }
                break;
        }

        String header = "[-" + HEADER_CMD + ";" + CMD_NATIVE + "]";
        String blockComment = header + content;
        nativeMethod.setBlockComment(blockComment);
    }

    private static String getOperator(String operatorCode, String param) {
        String oper = "";
        if(!operatorCode.isEmpty()) {
            if(operatorCode.equals("[]")) {
                oper = "(*nativeObject)[" + param + "]";
            } else {
                oper = "(*nativeObject " + operatorCode + " " + param + ")";
            }
        }
        return oper;
    }

    private static String getParams(IDLMethod idlMethod, MethodDeclaration methodDeclaration) {
        NodeList<Parameter> parameters = methodDeclaration.getParameters();
        ArrayList<IDLParameter> idParameters = idlMethod.parameters;
        return getParams(parameters, idParameters);
    }

    private static String getParams(NodeList<Parameter> parameters, ArrayList<IDLParameter> idParameters) {
        String param = "";
        for(int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            IDLParameter idlParameter = idParameters.get(i);
            Type type = parameter.getType();
            String paramName = getParam(idlParameter, type);
            if(i > 0) param += ", ";
            param += paramName;
        }
        return param;
    }

    private static String getParam(IDLParameter idlParameter, Type type) {
        IDLFile idlFile = idlParameter.idlFile;
        String paramName = idlParameter.name;
        String cppType = idlParameter.getCPPType();
        String classType = cppType;
        boolean isEnum = idlParameter.isEnum();
        boolean isAny = idlParameter.isAny;
        boolean isRef = idlParameter.isRef;
        boolean isValue = idlParameter.isValue;
        boolean isArray = idlParameter.isArray;
        boolean isObject = type.isClassOrInterfaceType();

        if(!isEnum && isObject && !classType.equals("char*")) {
            paramName += IDLDefaultCodeParser.NATIVE_PARAM_ADDRESS;
            if(isArray) {
                String idlType = cppType.replace("[]", "*");
                if(idlParameter.idlClassOrEnum != null && !isRef) {
                    idlType += "*";
                }
                paramName = "(" + idlType + ")" + paramName;
            } else {
                String idlArrayOrNull = IDLHelper.getIDLArrayClassOrNull(classType);
                if(idlArrayOrNull != null) {
                    classType = idlArrayOrNull;
                }
                IDLClass paramClass = idlFile.getClass(classType);
                if(paramClass != null) {
                    classType = paramClass.getCPPName();
                }
                if(isRef || isValue) {
                    paramName = "*((" + classType + "* )" + paramName + ")";
                } else if(isAny) {
                    paramName = "(" + classType + ")" + paramName;
                } else {
                    paramName = "(" + classType + "* )" + paramName;
                }
            }
        } else if(isAny) {
            paramName = "( void* )" + paramName;
        } else {
            if(classType.equals("int")) {
                paramName = "(int)" + paramName;
            } else if(classType.equals("float")) {
                paramName = "(float)" + paramName;
            } else if(classType.equals("double")) {
                paramName = "(double)" + paramName;
            } else if(classType.equals("boolean")) {
                paramName = "(bool)" + paramName;
            }
        }

        IDLEnumClass anEnum = idlFile.getEnum(classType);
        if(anEnum != null) {
            if(anEnum.typePrefix.equals(classType)) {
                paramName = "(" + classType + ")" + paramName;
            } else {
                paramName = "(" + anEnum.typePrefix + "::" + classType + ")" + paramName;
            }
        }
        return paramName;
    }

    private void ensureCallbackUpcallMembers(ClassOrInterfaceDeclaration classDeclaration,
                                             ArrayList<Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>>> methods) {
        if(classDeclaration.getFieldByName(CALLBACK_UPCALL_ARENA_FIELD).isEmpty()) {
            classDeclaration.addField("Arena", CALLBACK_UPCALL_ARENA_FIELD, Modifier.Keyword.PRIVATE);
        }
        for(Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> pair : methods) {
            String stubFieldName = getCallbackStubFieldName(pair.a.getCPPName());
            if(classDeclaration.getFieldByName(stubFieldName).isEmpty()) {
                classDeclaration.addField("MemorySegment", stubFieldName, Modifier.Keyword.PRIVATE);
            }
        }

        if(classDeclaration.getMethodsByName(CALLBACK_RELEASE_METHOD).isEmpty()) {
            MethodDeclaration releaseMethod = classDeclaration.addMethod(CALLBACK_RELEASE_METHOD, Modifier.Keyword.PRIVATE);
            StringBuilder methodBody = new StringBuilder();
            methodBody.append("{\n");
            methodBody.append("    Arena arena = ").append(CALLBACK_UPCALL_ARENA_FIELD).append(";\n");
            for(Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> pair : methods) {
                methodBody.append("    ").append(getCallbackStubFieldName(pair.a.getCPPName())).append(" = null;\n");
            }
            methodBody.append("    ").append(CALLBACK_UPCALL_ARENA_FIELD).append(" = null;\n");
            methodBody.append("    if(arena != null) {\n");
            methodBody.append("        try {\n");
            methodBody.append("            arena.close();\n");
            methodBody.append("        } catch(Exception ignored) {\n");
            methodBody.append("        }\n");
            methodBody.append("    }\n");
            methodBody.append("}\n");
            releaseMethod.setBody(StaticJavaParser.parseBlock(methodBody.toString()));
        }
    }

    private void ensureDeleteNativeReleasesUpcalls(ClassOrInterfaceDeclaration classDeclaration) {
        List<MethodDeclaration> deleteMethods = classDeclaration.getMethodsBySignature("deleteNative");
        if(deleteMethods.size() != 1) return;
        MethodDeclaration deleteMethod = deleteMethods.get(0);
        if(deleteMethod.getBody().isEmpty()) return;
        BlockStmt body = deleteMethod.getBody().get();
        if(body.toString().contains(CALLBACK_RELEASE_METHOD + "()")) return;
        body.addStatement(CALLBACK_RELEASE_METHOD + "();");
    }

    private String getCallbackStubFieldName(String callbackMethodName) {
        return "upcallStub_" + callbackMethodName;
    }
}


