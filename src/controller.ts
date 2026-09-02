import { CPFService } from "./cpf/cpf-service.js";
import { CPFValidator } from "./cpf/cpf-validator.js";
import { Http } from "./http.js";
import { TokenService } from "./jwt/token-service.js";
import { RouteHandlerContext } from "./router.js";

export interface Services {
  cpfService: CPFService;
  tokenService: TokenService;
}

export class Controller {
    public static async validateCPF({ event }: RouteHandlerContext, services: Services) {
        const { cpf } = Http.parseBody(event);
        if (typeof cpf !== 'string' || !cpf.trim()) {
            return Http.response(400, { error: 'cpf is required' });
        }
        return Http.response(200, { cpf: cpf.trim(), valid: CPFValidator.isValid(cpf) });
    }

    public static async isCPFDuplicated({ pathParameters }: RouteHandlerContext, services: Services) {
        const cpf = pathParameters['cpf'];
        if (!CPFValidator.isValid(cpf)) {
            return Http.response(400, { error: 'invalid cpf' });
        }
        const duplicate = await services.cpfService.isDuplicate(cpf);
        return Http.response(200, { cpf, duplicate });
    }

    public static async issueJWT({ event }: RouteHandlerContext, services: Services) {
        const { cpf, subject } = Http.parseBody(event);
        if (typeof cpf !== 'string' || !CPFValidator.isValid(cpf)) {
            return Http.response(400, { error: 'a valid cpf is required' });
        }
        const token = await services.tokenService.sign({
            subject: typeof subject === 'string' && subject ? subject : cpf,
            cpf,
        });
        return Http.response(200, { token });
    }

    public static async getJWKS(_: RouteHandlerContext, services: Services) {
        return Http.response(200, services.tokenService.jwks())
    }
}