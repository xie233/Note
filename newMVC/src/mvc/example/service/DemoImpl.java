package mvc.example.service;


import mvc.core.NPService;

@NPService
public class DemoImpl implements IDemoService {

    @Override
    public String get(String name) {
        return name+ " is 18";
    }
}
